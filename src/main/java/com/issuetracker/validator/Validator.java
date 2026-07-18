package com.issuetracker.validator;

import java.time.LocalDate;

import com.issuetracker.exception.IssueTrackerException;
import com.issuetracker.model.Issue;
import com.issuetracker.model.IssueStatus;

//Do Not Change Any Signature
public class Validator {

 public void validate(Issue issue) throws IssueTrackerException {
	// Your Code Goes Here
     if(!isValidIssueId(issue.getIssueId())) {
         throw new IssueTrackerException("The issue ID is of invalid format!  ");
     }
     if (!isValidIssueDescription(issue.getIssueDescription())) {
         throw new IssueTrackerException("The issue description is of unacceptable format!");
     }
     if (!isValidReportedOn(issue.getReportedOn())) {
         throw new IssueTrackerException("The reported date is incorrect!  ");
     }
     if (!isValidStatus(issue.getStatus())) {
         throw new IssueTrackerException("The status of the issue is inappropriate! ");
     }
 }

 public Boolean isValidIssueId(String issueId)
 {
	// Your Code Goes Here
     String regex= "^(MTI-I)-[0-9]{2}[1-9]-(LS|MS|HS)$";
	return issueId.matches(regex);

 }

 public Boolean isValidIssueDescription(String issueDescription)
 {
	// Your Code Goes Here
     if (issueDescription==null || issueDescription.isEmpty()||issueDescription.trim().isEmpty()){
         return false;
     }
     if (issueDescription.length() > 50) {
         return false;
     }

     // Check regex pattern
     return issueDescription.matches("^[A-Za-z]+(?: [A-Za-z]+)*$");
 }

 public Boolean isValidReportedOn(LocalDate reportedOn)
 {
	// Your Code Goes Here
     return !reportedOn.isAfter(LocalDate.now());
 }

 public Boolean isValidStatus(IssueStatus status)
 {
	// Your Code Goes Here
     return status != null && status != IssueStatus.RECALLED && status != IssueStatus.CLOSED && status != IssueStatus.RESOLVED;
 }
}