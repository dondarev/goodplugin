package com.deretsandcompany.goodplugin.service;

import com.atlassian.jira.plugin.report.ReportModuleDescriptor;
import com.atlassian.jira.web.action.ProjectActionSupport;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
public class ReportContextRunChart {
    private ReportModuleDescriptor descriptor;
    private ProjectActionSupport action;
    private boolean statuscontext = true;
    private Long projectID;
    Timestamp dateOne = new Timestamp(System.currentTimeMillis()-(1000*60*60*24*7));
    Timestamp dateTwo= new Timestamp(System.currentTimeMillis());

    public Timestamp getDateOne() {
        return dateOne;
    }

    public void setDateOne(Timestamp dateOne) {
        this.dateOne = dateOne;
    }

    public Timestamp getDateTwo() {
        return dateTwo;
    }

    public void setDateTwo(Timestamp dateTwo) {
        this.dateTwo = dateTwo;
    }

    public ReportModuleDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(ReportModuleDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public ProjectActionSupport getAction() {
        return action;
    }

    public void setAction(ProjectActionSupport action) {
        this.action = action;
    }

    public void setStatuscontext(boolean statuscontext) {
        this.statuscontext = statuscontext;
    }

    public boolean isStatuscontext() {
        return statuscontext;
    }

    public Long getProjectID() {
        return projectID;
    }

    public void setProjectID(Long projectID) {
        this.projectID = projectID;
    }
}
