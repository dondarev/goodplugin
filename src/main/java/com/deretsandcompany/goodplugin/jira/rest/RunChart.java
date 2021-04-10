package com.deretsandcompany.goodplugin.jira.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.report.ReportModuleDescriptor;
import com.atlassian.jira.plugin.report.impl.AbstractReport;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.query.Query;
import com.deretsandcompany.goodplugin.service.ReportContextRunChart;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.sql.Timestamp;
import java.util.*;


@Path("/")
@Scanned
public class RunChart extends AbstractReport {
    @JiraImport
    private final SearchProvider searchProvider;
    @JiraImport
    private final ProjectManager projectManager;

    @Autowired
    ReportContextRunChart reportContextRunChart;

    private Long projectId;
    private static final Logger log = Logger.getLogger("Run chart");


    public RunChart(SearchProvider searchProvider, ProjectManager projectManager) {
        this.searchProvider = searchProvider;
        this.projectManager = projectManager;
    }

    @GET
    @Path("/RunChart")
    public String data(@Context UriInfo info) throws Exception {
        String dateOne = info.getQueryParameters().getFirst("dateOne");
        String dateTwo = info.getQueryParameters().getFirst("dateTwo");
        reportContextRunChart.setDateOne(new Timestamp(Long.parseLong(dateOne)));
        reportContextRunChart.setDateTwo(new Timestamp(Long.parseLong(dateTwo)));
        return dateOne;
    }

    @Override
    public String generateReportHtml(ProjectActionSupport action, Map map) throws Exception {
        if (reportContextRunChart.isStatuscontext()) {
            Map<String, Object> data = getData(action.getLoggedInUser(), reportContextRunChart.getDateOne(), reportContextRunChart.getDateTwo(), projectId);
            setContext(descriptor, action);
            return descriptor.getHtml("runchart", data);
        } else {
            Map<String, Object> data = getDataCUST(reportContextRunChart.getAction().getLoggedInUser(), reportContextRunChart.getProjectID());
            return reportContextRunChart.getDescriptor().getHtml("runchart", data);
        }


    }

    private Map<String, Object> getDataCUST(ApplicationUser user, Long projectID) throws SearchException {
        JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
        Date startDate = new Date(reportContextRunChart.getDateOne().getTime());
        Date endDate = new Date(reportContextRunChart.getDateTwo().getTime());
        Query query = queryBuilder.where().project(projectId).and().resolutionDateBetween(startDate, endDate).buildQuery();
        SearchResults search = searchProvider.search(query, user, new PagerFilter());
        List<Issue> issues = search.getIssues();
        List<Long> periodIssueLive = new ArrayList<>();
        List<String> tasks = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        for (Issue issue : issues) {
            Timestamp created = issue.getCreated();
            Timestamp resolutionDate = issue.getResolutionDate();
            resolutionDate = new Timestamp(resolutionDate.getTime() + (1000 * 60 * 60 * 24 * 7));
            tasks.add(issue.getKey());
            long l = ((resolutionDate.getTime()) - created.getTime()) / (1000 * 60 * 60 * 24);
            periodIssueLive.add(l);
        }
        Project projectObj = null;
        projectObj = projectManager.getProjectObj(projectId);
        map.put("periodIssueLives", periodIssueLive);
        map.put("dates", tasks);
        map.put("projectName", projectObj.getKey());
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("interval", 1);
        return map;
    }

    private void setContext(ReportModuleDescriptor descriptor, ProjectActionSupport action) {
        if (reportContextRunChart.isStatuscontext()) {
            reportContextRunChart.setDescriptor(descriptor);
            reportContextRunChart.setAction(action);
            reportContextRunChart.setProjectID(projectId);
            reportContextRunChart.setStatuscontext(false);
            reportContextRunChart.setDateOne(new Timestamp(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 30)));
            reportContextRunChart.setDateTwo(new Timestamp(System.currentTimeMillis()));
        }
    }

    private Map<String, Object> getData(ApplicationUser user, Date startDate, Date endDate, Long projectId) throws SearchException {
        JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
        Query query = queryBuilder.where().project(projectId).and().resolutionDateBetween(startDate, endDate).buildQuery();
        SearchResults search = searchProvider.search(query, user, new PagerFilter());
        List<Issue> issues = search.getIssues();
        List<Long> periodIssueLive = new ArrayList<>();
        List<String> tasks = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        for (Issue issue : issues) {
            Timestamp created = issue.getCreated();
            Timestamp resolutionDate = issue.getResolutionDate();
            resolutionDate = new Timestamp(resolutionDate.getTime() + (1000 * 60 * 60 * 24 * 7));
            tasks.add(issue.getKey());
            long l = ((resolutionDate.getTime()) - created.getTime()) / (1000 * 60 * 60 * 24);
            periodIssueLive.add(l);
        }
        Project projectObj = null;
        projectObj = projectManager.getProjectObj(projectId);
        map.put("periodIssueLives", periodIssueLive);
        map.put("dates", tasks);
        map.put("projectName", projectObj.getKey());
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("interval", 1);
        return map;
    }

    public void validate(ProjectActionSupport action, Map params) {
        projectId = ParameterUtils.getLongParam(params, "selectedProjectId");
        if (projectId == null || projectManager.getProjectObj(projectId) == null) {
            action.addError("selectedProjectId", action.getText("report.issuecreation.projectid.invalid"));
            log.error("Invalid projectId");
        }
    }
}
