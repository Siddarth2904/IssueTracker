package com.issuetracker.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.issuetracker.dao.AssigneeDAO;

import com.issuetracker.dao.AssigneeDAOImpl;
import com.issuetracker.model.Assignee;
import com.issuetracker.model.Unit;

public class AssigneeServiceImpl implements AssigneeService
{
    private AssigneeDAO assigneeDAO;

    public AssigneeServiceImpl() {
        this.assigneeDAO = new AssigneeDAOImpl() {
        };
    }

    /**
     * @params
     *         unit - The assignee unit
     * 
     * @operation Fetches the assignees list for the given unit
     * 
     * @returns
     *          List<Assignee> - List of assignees fetched
     */
    @Override
    public List<Assignee> fetchAssignee(Unit unit) 
    {
	// Your Code Goes Here
        List<Assignee> assignees=null;
        assignees=assigneeDAO.fetchAssignees(unit);
        return assignees.stream()
                .filter(assignee -> assignee.getNumberOfIssuesActive() < 3)
                .collect(Collectors.toList());
    }

    /**
     * @params
     *         assigneeEmail - The assignee email id
     *         operation - The operation code
     * 
     * @operation Updates the active issues count for the given assignee email,
     *            by incrementing or decrementing it based on the operation code
     * 
     */
    @Override
    public void updateActiveIssueCount(String assigneeEmail,
				       Character operation)
    {
	// Your Code Goes Here
        Assignee assignee = assigneeDAO.getAssigneeByEmail(assigneeEmail);

        if (assignee == null) {
            throw new IllegalArgumentException("Assignee not found with email: " + assigneeEmail);
        }

        int currentCount = assignee.getNumberOfIssuesActive();

        if (operation == 'I') {
            assignee.setNumberOfIssuesActive(currentCount + 1);
        } else if (operation == 'D') {
            assignee.setNumberOfIssuesActive(Math.max(0, currentCount - 1));
        }
    }
}