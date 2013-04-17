<#include "/s_html.ftl" >
<head>
<#include "/s_head.ftl" >
<script type='text/javascript' src='${xmet_root}/jquery/jquery.validate.min.js'></script>

<script type='text/javascript'>
$(document).ready(function() {
	$.ajaxSetup({cache:false});//while dev
	$("#pwdForm").validate({
		rules : {
			'pwd1': {
				required : true,
				minlength: 6
			},
			'pwd2': {
				required : true,
				minlength: 6,
				equalTo: "#pwd1"
			}
			
		},
		messages : {
			'pwd1'   : {
				required: "Please provide a new password",
				minlength: "Your password must be at least 6 characters long"
			},
			'pwd2'   : {
				required: "Please provide a new password",
				minlength: "Your password must be at least 6 characters long",
				equalTo: "Please enter the same password as above"
			}			
		}
	});	
	<#if xmet_reg_confirmed??>
	    $.ajax({
	        dataType: "json",
	        async: false,
	        url: "${xmet_root}/forgotten/confirm?code=${xmet_reg_confirmed}&media=application%2Fjson",
	        success: function(data, status, xhr) {
	           	$.each(data["confirmation"],function(index, entry) {
	        		if ('confirmed' == entry["status"]) { 
	        			$("#failure").hide();
	        			$(".success").show();
	        		} else {
	        			$("#failure").hide();
	        			$(".success").show();	        			
	        		}
	        	});
	        },
	        statusCode: {
	            404: function() {
	        		$("#failure").show();
	        		$(".success").hide();
	            }
	        },
	        error: function(xhr, status, err) {
	       		$("#failure").show();
        		$(".success").hide();
        		$("#progress").hide();
	        },
	        complete: function(xhr, status) {
	        	$("#progress").hide();
	        }
	     });		
	</#if>
});
</script>

<script type="text/javascript">
jQuery(document).ready(function()
{
    jQuery("#breadCrumb ul").append('<li><a href="${xmet_root}/forgotten" title="XMetDB password reset">Password reset</a></li>');
    jQuery("#breadCrumb ul").append('<li><a href="${xmet_root}/forgotten/notify" title="XMetDB password reset">Confirm</a></li>');
    jQuery("#breadCrumb").jBreadCrumb();
    loadHelp("${xmet_root}","pwd_forgotten");
})
</script>
</head>
<body>

<div class="container columns" style="margin:0;padding:0;">
		<#include "/s_banner.ftl">
	 	<#include "/s_menu.ftl">
		
		<!-- Page Content
		================================================== -->
		<div class="twelve columns add-bottom" style="padding:0;" >	

		<div class='ui-widget-header ui-corner-top' >&nbsp;XMetDB Password reset</div>
	    <div class='ui-widget-content ui-corner-bottom'>	
	    	<div class='row'></div>
			<#if xmet_reg_confirmed??>
			
			<form action="${xmet_root}/forgotten/confirm?code=${xmet_reg_confirmed}&method=PUT" id="pwdForm"  method="POST" >	
			<div class='success row half-bottom' style="margin:5px;padding:5px;"> 	
				<label class='three columns alpha' for="pwd1">New password</label>
				<input class="five columns alpha half-bottom" type='password' size='40' id='pwd1' name='pwd1' value=''/>
				<div class="eight columns omega">&nbsp;</div>
			</div>		
			<div class='success row half-bottom' style="margin:5px;padding:5px;"> 	
				<label class='three columns alpha' for="pwd2">Confirm new password</label>
				<input class="five columns alpha half-bottom"  type='password' size='40' id='pwd2' name='pwd2' value=''/>
				<div class="eight columns omega">&nbsp;</div>
			</div>		
			<div class='success row half-bottom' style="margin:5px;padding:5px;"> 	
				<label class='three columns alpha' >&nbsp;</label>
				<input class="three columns alpha half-bottom" id='updatepwd' name='updatepwd' type='submit' class='submit' value='Update'>
				<div class="eleven columns omega half-bottom">&nbsp;</div>
			</div>				
			</form>	
				

			<div id='failure' class='row' style='display:none'>
				<label class='three columns alpha' >&nbsp;</label>
				<strong class='thirteen columns alpha' >Invalid confirmation code!</strong>
			</div>
			
			<img id='progress' src="${xmet_root}/images/progress.gif" alt="Please wait..." title="Please wait...">
			
			<#else>
			<div  class='row' style="text-align:center;">
			<span class="ui-icon ui-icon-info" style="display:inline-block"></span>
			Please follow the instructions in the password reset e-mail.
			</div>
			</#if>
			<div class='row'></div>
		</div>
		
	 
		<!-- twelve -->
		</div>
		
		
		<!-- Right column and footer
		================================================== -->
		<#include "/s_help.ftl">
		<#include "/s_footer.ftl">
		
</div><!-- container -->

		<#include "/scripts-welcome.ftl">
</body>
</html>