<#include "/s_html.ftl" >
<head>
<#include "/s_head.ftl" >
<#include "/users_head.ftl" >

<script type='text/javascript'>
$(document).ready(function() {
	var oTable = defineUsersTable("${xmet_root}","${xmet_request_json}");
});
</script>

</head>
<body>

<div class="container columns" style="margin:0;padding:0;">
		<#include "/s_banner.ftl">
	 	<#include "/s_menu.ftl">
		
		<!-- Page Content
		================================================== -->
		<div class="twelve columns" style="padding:0;" >
		
		<table id='users' cellpadding='0' border='0' width='100%' cellspacing='0' style="margin:0;padding:0;" >
		<thead>	
		<th>Name</th>
		<th>Affiliation</th>
		<th>e-mail</th>
		<th>Keywords</th>
		<th>Reviewer</th>
		</thead>
		<tbody></tbody>
		</table>
			
		</div> 
		
		<!-- Right column and footer
		================================================== -->
		<#include "/s_help.ftl">
		<#include "/s_footer.ftl">
		
</div><!-- container -->

		<#include "/scripts-welcome.ftl">
</body>
</html>