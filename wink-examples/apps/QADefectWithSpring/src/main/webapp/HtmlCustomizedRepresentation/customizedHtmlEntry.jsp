<%@page import="org.apache.wink.example.qadefect.legacy.DefectBean"%>
<%@page import="org.apache.wink.example.qadefect.legacy.TestBean"%>
<%@page import="java.util.List"%>
<%@page import="org.apache.wink.example.qadefect.resources.DefectsResource"%><html>
<head>
<STYLE TYPE="text/css"> 
<%@include file="css/customized.css" %>  
</STYLE>
</head>
<body>
<%
		DefectBean defectBean = (DefectBean)request.getAttribute(DefectsResource.CUSTOMIZED_JSP_ATTR);
		List<TestBean> testList = defectBean.getTests();
		
		String borderSize="0";
%>
<table width="100%" border="<%=borderSize %>"> 	
	<tr>
		<td class="page-title"><u>QC application - Defect Details</u> (customized application)</td>
	</tr> 
	<tr><td>&nbsp;</td></tr>
	<tr>
		<td class="page-title"><%= defectBean.getName()%></td>
	</tr> 
	<tr>
		<td>
			<table cellpadding="0" cellspacing="0">
				<tr>
						<td colspan="2" class="column-data column-data-border">&nbsp;</td>
				</tr>
				<tr>		
						<td class="column-data-blue column-data-border-left-right">Id</td>
						<td class="column-data column-data-border-right"><%= defectBean.getId()%></td>
				</tr>
				<tr>		
						<td class="column-data-blue column-data-border-left-right">Description</td>
						<td class="column-data column-data-border-right"><%= defectBean.getDescription()%></td>
				</tr>
				<tr>
					<td class="column-data-blue column-data-border-left-right">Severity</td>
					<td class="column-data column-data-border-right"> <%= defectBean.getSeverity()%></td>
				</tr>
				<tr>
					<td class="column-data-blue column-data-border-left-right">Status</td>
					<td class="column-data column-data-border-right">  <%= defectBean.getStatus()%></td>
				</tr>
				<tr>
					<td class="column-data-blue column-data-border-left-right">Created on</td>
					<td class="column-data column-data-border-right"><%= defectBean.getCreated()%></td>
				</tr>
				<tr>
					<td class="column-data-blue column-data-border-left-right">Author</td>
					<td class="column-data column-data-border-right"><%= defectBean.getAuthor()%></td>
				</tr>
				<tr>
					<td class="column-data-blue column-data-border-left-right">Assigned to</td>
					<td class="column-data column-data-border-right"><%= defectBean.getAssignedTo()%></td>
				</tr>
				<tr>
					<td class="column-data-blue column-data-border-left-right">Tests</td>
					<td class="column-data column-data-border-right">
						<% if(testList != null && defectBean.getTests().size()>0) { %>				
							 The defect has <%= defectBean.getTests().size()%> 
							<%	 if(defectBean.getTests().size() == 1) { %>
								 	test									 
								<% } else { %>
									tests
								<% } %>				
						<%} else { %>
							The defect has no tests available.
						<% } %>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
</body>
</html>