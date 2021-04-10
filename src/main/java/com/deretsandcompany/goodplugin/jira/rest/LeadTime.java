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
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.query.Query;
import com.deretsandcompany.goodplugin.service.ReportContextLeadTime;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.sql.Timestamp;
import java.util.*;

@Path("/data")
public class LeadTime extends AbstractReport {

    @JiraImport
    private final SearchProvider searchProvider;
    @JiraImport
    private final ProjectManager projectManager;

    @Autowired
    ReportContextLeadTime reportContext;


    private Long projectId;
    private static final Logger log = Logger.getLogger("LeadTime");


    public LeadTime(SearchProvider searchProvider, ProjectManager projectManager) {
        this.searchProvider = searchProvider;
        this.projectManager = projectManager;

    }


    @GET
    @Path("/hi")
    public String data(@Context UriInfo info) throws Exception {
        String dateOne = info.getQueryParameters().getFirst("dateOne");
        String dateTwo = info.getQueryParameters().getFirst("dateTwo");
        reportContext.setDateOne(new Timestamp(Long.parseLong(dateOne)));
        reportContext.setDateTwo(new Timestamp(Long.parseLong(dateTwo)));
        return dateOne;
    }

    private void setContext(ReportModuleDescriptor descriptor, ProjectActionSupport action) {
        if (reportContext.isStatuscontext()) {
            reportContext.setDescriptor(descriptor);
            reportContext.setAction(action);
            reportContext.setProjectID(projectId);
            reportContext.setStatuscontext(false);
            reportContext.setDateOne(new Timestamp(System.currentTimeMillis()-(1000*60*60*24*30)));
            reportContext.setDateTwo(new Timestamp(System.currentTimeMillis()));
        }
    }

    @Override
    public String generateReportHtml(ProjectActionSupport action, Map map) throws Exception {
        if (reportContext.isStatuscontext()) {
            Map<String, Object> data = getData(action.getLoggedInUser(), projectId);
            setContext(descriptor, action);
            return descriptor.getHtml("leadtime", data);
        }else {
            Map<String, Object> data = getDataCUST(reportContext.getAction().getLoggedInUser(), reportContext.getProjectID());
            return reportContext.getDescriptor().getHtml("leadtime", data);
        }
    }

    private Map<String, Object> getData(ApplicationUser user, Long projectId) throws SearchException {
        JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
        Query query = queryBuilder.where().project(projectId).buildQuery();
        SearchResults search = searchProvider.search(query, user, new PagerFilter());
        List<Issue> issues = search.getIssues();
        List<Long> leadTimeDay = Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L
                , 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L);
        List<Integer> countResolTask = new ArrayList<>();

        for (Long aLong : leadTimeDay) {
            int countRT = 0;
            for (Issue issue : issues) {
                Timestamp created = issue.getCreated();
                Timestamp resolutionDate = issue.getResolutionDate();
                if (resolutionDate != null) {
                    long resolutionDaysIssue = ((resolutionDate.getTime()) - created.getTime()) / (1000 * 60 * 60 * 24);
                    if (resolutionDaysIssue == aLong) {
                        countRT++;
                    }
                }
            }
            countResolTask.add(countRT);
        }

        Map<String, Object> map = new HashMap<>();
        Project projectObj;
        projectObj = projectManager.getProjectObj(projectId);
        map.put("data", countResolTask);
        map.put("leadTimeDay", leadTimeDay);
        map.put("countResolTask", countResolTask);
        map.put("projectName", projectObj.getKey());
        return map;
    }


    private Map<String, Object> getDataCUST(ApplicationUser user, Long projectId) throws SearchException {
        JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
        Date startDate = new Date(reportContext.getDateOne().getTime());
        Date endDate = new Date(reportContext.getDateTwo().getTime());
        Query query = queryBuilder.where().project(projectId).and().resolutionDateBetween(startDate, endDate).buildQuery();
        SearchResults search = searchProvider.search(query, user, new PagerFilter());
        List<Issue> issues = search.getIssues();
        List<Long> leadTimeDay = new ArrayList<>();
        long countDay = (reportContext.getDateTwo().getTime() - reportContext.getDateOne().getTime())/(1000 * 60 * 60 * 24);
        for (long i=0;i<countDay;i++){
            leadTimeDay.add(i);
        }
        List<Integer> countResolTask = new ArrayList<>();

        for (Long aLong : leadTimeDay) {
            int countRT = 0;
            for (Issue issue : issues) {
                Timestamp created = issue.getCreated();
                Timestamp resolutionDate = issue.getResolutionDate();
                if (resolutionDate != null) {
                    long resolutionDaysIssue = ((resolutionDate.getTime()) - created.getTime()) / (1000 * 60 * 60 * 24);
                    if (resolutionDaysIssue == aLong) {
                        countRT++;
                    }
                }
            }
            countResolTask.add(countRT);
        }

        Map<String, Object> map = new HashMap<>();
        Project projectObj;
        projectObj = projectManager.getProjectObj(projectId);
        map.put("data", countResolTask);
        map.put("leadTimeDay", leadTimeDay);
        map.put("countResolTask", countResolTask);
        map.put("projectName", reportContext.getDateOne().toString()+"-"+reportContext.getDateTwo().toString());
        return map;
    }

    @Override
    public void validate(ProjectActionSupport action, Map params) {
        projectId = ParameterUtils.getLongParam(params, "selectedProjectId");
        if (projectId == null || projectManager.getProjectObj(projectId) == null) {
            action.addError("selectedProjectId", action.getText("report.issuecreation.projectid.invalid"));
            log.error("Invalid projectId");
        }
    }
}
