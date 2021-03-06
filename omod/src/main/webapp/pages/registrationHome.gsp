<%
	ui.decorateWith("kenyaemr", "standardPage", [ layout: "sidebar" ])
%>

<style type="text/css">
#end-of-day {
	display: none;
}
#end-of-day .spaced {
	padding: 0.3em;
}
#calendar .ui-widget-content {
	border: 0;
	background: inherit;
	padding: 0;
}
</style>

<div id="content-side">
	${ ui.includeFragment("kenyaui", "widget/panelMenu", [
		heading: "Tasks",
		items: [
			[ iconProvider: "kenyaui", icon: "buttons/patient_search.png", label: "Search for a Patient", href: ui.pageLink("kenyaemr", "registrationSearch") ]
		]
	]) }

	${ ui.decorate("kenyaui", "panel", [ heading: "Select Day to View" ], """<div id="calendar"></div>""") }

	<div class="ke-panel-frame" id="end-of-day">
		<div class="ke-panel-heading">End of Day</div>

		<div class="ke-panel-content">
			Close all open visits of the following types:
			<form id="close-visits-form" method="post" action="${ ui.actionLink("kenyaemr", "registrationUtil", "closeActiveVisits") }">
				<div class="form-data"></div>
				<input type="submit" value="Close Visits" />
				<div class="global-error-container" style="display: none">
					${ ui.message("fix.error.plain") }
					<ul class="global-error-content"></ul>
				</div>
			</form>
		</div>
	</div>
</div>

<div id="content-main">

	${ ui.includeFragment("kenyaemr", "dailySchedule", [ id: "schedule", page: "registrationViewPatient", date: scheduleDate ]) }

</div>

<script type="text/javascript">
	jq(function() {
		jq('#calendar').datepicker({
			dateFormat: 'yy-mm-dd',
			defaultDate: '${ new java.text.SimpleDateFormat("yyyy-MM-dd").format(scheduleDate) }',
			gotoCurrent: true,
			onSelect: function(dateText) {
				location.href = ui.pageLink('kenyaemr', 'registrationHome', { scheduleDate: dateText });
			}
		});

		ui.setupAjaxPost('#close-visits-form', {
			onSuccess: function (result) {
				loadActiveVisitTypes();
				ui.notifySuccess(result.message);
			}
		});

		loadActiveVisitTypes();
	});

	function loadActiveVisitTypes() {
		jq.getJSON(ui.fragmentActionLink('kenyaemr', 'registrationUtil', 'activeVisitTypes'), function(result) {
			if (result.length == 0) {
				jq('#end-of-day').hide();
				return;
			}
			else {
				var str = "";
				for (var i = 0; i < result.length; ++i) {
					var r = result[i];
					str += '<div class="spaced"><input type="checkbox" name="visitType" value="' + r.visitTypeId + '"/> ' + r.name + ' (' + r.count + ')</div>';
				}
				jq('#end-of-day .form-data').html(str);
				jq('#end-of-day').show();
			}
		});
	}
</script>