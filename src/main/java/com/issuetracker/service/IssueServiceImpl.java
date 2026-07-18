package com.issuetracker.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.issuetracker.dao.IssueDAO;
import com.issuetracker.dao.IssueDAOImpl;
import com.issuetracker.exception.IssueTrackerException;
import com.issuetracker.model.Assignee;
import com.issuetracker.model.Issue;
import com.issuetracker.model.IssueReport;
import com.issuetracker.model.IssueStatus;
import com.issuetracker.validator.Validator;

public class IssueServiceImpl implements IssueService
{
    private AssigneeService assigneeService;

    private IssueDAO issueDAO;
    
    private Validator validator;
    /**
     * @params
     *         issue - The new issue to be reported
     * 
     * @operation Reports a new issue by
     *            * validating its details
     *            * fetch an assignee
     *            * persists the issue in the issueList
     * 
     * @returns
     *          String - The issue id
     */

    public IssueServiceImpl() {
        this.assigneeService = new AssigneeServiceImpl();
        this.issueDAO = new IssueDAOImpl();
        this.validator = new Validator();
    }
    @Override
    public String reportAnIssue(Issue issue) throws IssueTrackerException
    {
        // Your Code Goes Here
        validator.validate(issue);
        List<Assignee> availableAssignees = assigneeService.fetchAssignee(issue.getIssueUnit());
        if (availableAssignees != null && !availableAssignees.isEmpty()) {
            // Pick the first available assignee
            Assignee selectedAssignee = availableAssignees.get(0);
            issue.setAssigneeEmail(selectedAssignee.getAssigneeEmail());
            // Increment the active issue count for the assignee
            assigneeService.updateActiveIssueCount(selectedAssignee.getAssigneeEmail(), 'I');
        }
        String issueId = issueDAO.reportAnIssue(issue);

        if (issueId == null) {
            throw new IssueTrackerException("An issue with the same ID already exists!");
        }

        return issueId;
    }

    /**
     * @params
     *         issueId - The issue id
     *         status - The new status
     * 
     * @operation Updates the status of the given issue with the given status
     * 
     * @returns
     *          Boolean - Result of the status update
     */
    @Override
    public Boolean updateStatus(String issueId,
				IssueStatus status) throws IssueTrackerException
    {
	// Your Code Goes Here
        Issue issue = issueDAO.getIssueById(issueId);

        if (issue == null) {
            throw new IssueTrackerException("An issue with the given ID is not found!");
        }

        // Check if status is same as existing
        if (issue.getStatus() == status) {
            throw new IssueTrackerException("There is no change in the issue status!");
        }

        // Check for RECALLED status compatibility
        if (status == IssueStatus.RECALLED && issue.getStatus() != IssueStatus.OPEN) {
            throw new IssueTrackerException("The current issue status is incompatible for recall!");
        }

        // Update the status
        issueDAO.updateStatus(issue, status);

        // If status changed to not OPEN or IN PROGRESS, decrement active issue count
        if (status != IssueStatus.OPEN && status != IssueStatus.IN_PROGRESS) {
            String assignee = issue.getAssigneeEmail();
            if (assignee != null) {
                assigneeService.updateActiveIssueCount(assignee, 'D');
            }
        }

        return true;
    }

    /**
     * @params
     *         filterCriteria - A map where its
     *         key denotes an attribute of the issue object
     *         value contains the filter value
     * 
     * @operation Generates a report of issues based on the filter criteria
     * 
     * @returns
     *          List<IssueReport> - The list of filtered issue objects
     */
    @Override
    public List<IssueReport> showIssues(Map<Character, Object> filterCriteria) throws IssueTrackerException
    {
	// Your Code Goes Here
        List<Issue> allIssues = issueDAO.getIssueList();

        if (allIssues == null || allIssues.isEmpty()) {
            throw new IssueTrackerException("No issues are found for the requested criteria!");
        }

        // Get the key and value from filterCriteria
        Character key = filterCriteria.keySet().iterator().next();
        Object value = filterCriteria.get(key);

        // Filter based on the criteria
        List<Issue> filteredIssues = allIssues.stream()
                .filter(issue -> {
                    if (key == 'A') {
                        // Filter by assignee email
                        String assigneeEmail = (String) value;
                        String assignee = issue.getAssigneeEmail();
                        return assigneeEmail.equals(assignee);
                    } else if (key == 'S') {
                        // Filter by status
                        IssueStatus status = (IssueStatus) value;
                        return issue.getStatus() == status;
                    }
                    return false;
                })
                .collect(Collectors.toList());

        if (filteredIssues.isEmpty()) {
            throw new IssueTrackerException("No issues are found for the requested criteria!");
        }

        // Convert Issue objects to IssueReport objects and return
        return filteredIssues.stream()
                .map(this::convertToIssueReport)
                .collect(Collectors.toList());

    }

    /**
     * @operation Deletes the issue object which are resolved or closed,
     *            at least 14 days ago
     * 
     * @returns
     *          List<Issue> - The list of issues which had been deleted
     */
    @Override
    public List<Issue> deleteIssues() throws IssueTrackerException
    {
	// Your Code Goes Here
        List<Issue> allIssues = issueDAO.getIssueList();

        if (allIssues == null || allIssues.isEmpty()) {
            throw new IssueTrackerException("No issues are old enough to be cleared!");
        }

        // Find issues that are RESOLVED or CLOSED and updated at least 14 days ago
        LocalDate fourteenDaysAgo = LocalDate.now().minusDays(14);

        List<Issue> issuesToDelete = allIssues.stream()
                .filter(issue -> {
                    IssueStatus status = issue.getStatus();
                    LocalDate updatedDate = issue.getUpdatedOn(); // Assuming getUpdatedDate() exists

                    return (status == IssueStatus.RESOLVED || status == IssueStatus.CLOSED)
                            && updatedDate != null
                            && updatedDate.isBefore(fourteenDaysAgo);
                })
                .collect(Collectors.toList());

        if (issuesToDelete.isEmpty()) {
            throw new IssueTrackerException("No issues are old enough to be cleared!");
        }

        // Remove the issues from the DAO
        List<Issue> issueList = issueDAO.getIssueList();
        issueList.removeAll(issuesToDelete);

        return issuesToDelete;
    }
    private IssueReport convertToIssueReport(Issue issue) {
        // Assuming IssueReport constructor or builder exists
        // Set other fields as needed
        return new IssueReport(issue.getIssueId(),issue.getIssueDescription(),issue.getAssigneeEmail(),issue.getStatus());
    }
}