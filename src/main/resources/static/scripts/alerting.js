function updateTaskGroup(groupcb) {
	$('.chosen-select-group').trigger("chosen:updated").chosen({
		width : "95%",
		no_results_text : 'Oops, no such Group!'
	}).on('change', function(evt, params) {
		var choosed = $('#inputGroupName_chosen .chosen-single span').text();
		groupcb(evt, choosed);
	});
}

function generateGroupSelect(topic, groups) {
	$('#create-consumer')
			.empty()
			.append(
					'<select data-placeholder="Choose a Consumer..." class="chosen-select chosen-select-group" tabindex="2" form="taskForm" name="group" id="inputGroupName"><option value=""></option></select>');
	var select = $('#taskForm .chosen-select-group');
	select.find('option').remove().end().append('<option value=""></option>');
	$.ajax({
		url : "alerting/tasks",
		type : "GET",
		success : function(tasks) {
			$.each(groups, function(index, groupName) {
				var alreadyInTask = false;
				tasks.forEach(function(task) {
					if (task.group === groupName && task.topic === topic) {
						alreadyInTask = true;
					}
				});
				if (!alreadyInTask) {
					select.append('<option value="' + groupName + '">'
							+ groupName + '</option>');
				}
			});
			$('.chosen-select-group').chosen({
				width : "100%",
				no_results_text : 'Oops, no such Consumer!'
			}).change(function() {
				// TODO Is there anything to do after group selected?
			});
		}
	});
}

function formArrToObject(formArray) {
	var obj = {};
	$.each(formArray, function(i, pair) {
		var cObj = obj, pObj, cpName;
		$.each(pair.name.split("."), function(i, pName) {
			pObj = cObj;
			cpName = pName;
			cObj = cObj[pName] ? cObj[pName] : (cObj[pName] = {});
		});
		pObj[cpName] = pair.value;
	});
	return obj;
}

function onInputNumber(event) {
	if (!event.target.value || event.target.value === ""
			|| event.target.value < 1) {
		event.target.style.borderColor = "red";
	} else {
		event.target.style.borderColor = "green";
	}
}

function checkEmail(event, email) {
	var regex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
	if (regex.test(email)) {
		event.target.style.borderColor = "green";
	} else {
		event.target.style.borderColor = "red";
	}
}

function onInputEmail(event) {
	var emails = event.target.value;
	if (emails.indexOf(';') !== -1) {
		var emailsArr = emails.split(';');
		$.each(emailsArr, function(index, email) {
			checkEmail(event, email);
		});
	} else {
		checkEmail(event, emails);
	}
}