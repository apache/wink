<%@page import="org.apache.wink.example.qadefect.legacy.DataStore"%>
<%@page import="org.apache.wink.example.qadefect.legacy.TestBean"%>
<%@page import="java.util.Collection"%>
<html>
<head>
<STYLE TYPE="text/css"> 
<%@include file="css/application.css" %>  
</STYLE>
</head>
<body>
<%
		// initialize the memory store
        DataStore store = DataStore.getInstance();

        // create data object (populated with store data)
        Collection<TestBean> testCollection = store.getTests();
		String borderSize="0";
%>
<table width="100%" border="<%=borderSize%>">
	<tr>
		<td class="page-title"><u>QC application - Test List</u> (legacy application)</td>
	</tr> 		
	<tr>
		<td>
			<table cellpadding="0" cellspacing="0">								
				<tr>
					<td colspan="7" class="column-data column-data-border">&nbsp;</td>
				</tr>
				<tr>	
							<td class="column-data-blue column-data-border-left-right">Id</td>
							<td class="column-data-blue column-data-border-right">Name</td>
							<td class="column-data-blue column-data-border-right">Description</td>
							<td class="column-data-blue column-data-border-right">Created</td>
							<td class="column-data-blue column-data-border-right">Author</td>
							<td class="column-data-blue column-data-border-right">Status</td>
							<td class="column-data-blue column-data-border-right">Defects</td>
				</tr>			
				
				<% 
				if (testCollection != null) {
					TestBean testBean = null;
					Object[] testArray = (Object[])testCollection.toArray();
					for(int i=0;i<testArray.length;i++) { 
					    testBean = (TestBean)testArray[i];
				%>				
					<tr>		
						<td class="column-data column-data-border-left-right"><%= testBean.getId()%></td>					
						<td class="column-data column-data-border-right"><%= testBean.getName()%></td>					
						<td class="column-data column-data-border-right"><%= testBean.getDescription()%></td>					
						<td class="column-data column-data-border-right"><%= testBean.getCreated()%></td>					
						<td class="column-data column-data-border-right"><%= testBean.getAuthor()%></td>					
						<td class="column-data column-data-border-right"><%= testBean.getStatus()%></td>					
					<% if(testBean.getDefects() != null) { %>					
						<td class="column-data column-data-border-right"> <%= testBean.getDefects().size()%></td>					
					<%} else { %>
						<td class="column-data column-data-border-right"> 0 </td>
					<% } %>
					</tr>
				<% } 
				
				}%>
			</table>
		</td>
	</tr>
</table>
</body>
</html>