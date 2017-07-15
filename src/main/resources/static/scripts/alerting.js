function newAlertInOffsetHistory() {
	$('#taskModal')
			.on(
					'show.bs.modal',
					function(event) {
						var button = $(event.relatedTarget);
						var topic = button.data('topic');
						var group = button.data('group');
						var modal = $(this);
						$("#inputTopicName").empty().append(
								'<p class="form-control">' + topic + '</p>');
						$('#create-consumer').empty().append(
								'<p class="form-control">' + group + '</p>');
						document.getElementById("inputThreshold").style.borderColor = "";
						document.getElementById("inputEmail").style.borderColor = "";
						modal.find('.modal-title').text('New Task');
						modal.find('.modal-body #topic').val(topic);
						modal.find('.modal-body #group').val(group);
					});

	$("#submitTask")
			.click(
					function() {
						var frm = $('#taskForm');
						var go = true;
						var inputThreshold = $("#inputThreshold").val();
						if (!inputThreshold || inputThreshold === "") {
							document.getElementById("inputThreshold").style.borderColor = "red";
							go = false;
						}
						var inputDiapause = $("#inputDiapause").val();
						if (!inputDiapause || inputDiapause === "") {
							document.getElementById("inputDiapause").style.borderColor = "red";
							go = false;
						}
						var inputEmail = $("#inputEmail").val();
						if (!inputEmail || inputEmail === "") {
							document.getElementById("inputEmail").style.borderColor = "red";
							go = false;
						}
						if (!go) {
							go = true;
							return;
						}

						var sendData = formArrToObject(frm.serializeArray());
						$.ajax({
							beforeSend : function(xhrObj) {
								xhrObj.setRequestHeader("Content-Type",
										"application/json");
								xhrObj.setRequestHeader("Accept",
										"application/json");
							},
							type : frm.attr('method'),
							url : frm.attr('action'),
							dataType : "json",
							data : JSON.stringify(sendData),
							success : function(response) {
								$('#taskModal').modal('hide');
								swal({
									title : "Task created!",
									type : "success",
									timer : 1000,
									showConfirmButton : false
								});
							}
						});
					});
}

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

function newAlert(reloadcb) {
	$('#taskModal')
			.on(
					'show.bs.modal',
					function(event) {
						var button = $(event.relatedTarget) // Button that
						var modal = $(this);
						$('.chosen-select-topic')
								.trigger("chosen:updated")
								.chosen({
									width : "100%",
									no_results_text : 'Oops, no such Topic!'
								})
								.on(
										'change',
										function(evt, params) {
											var choosedTopic = $(
													'#inputTopicName_chosen .chosen-single span')
													.text();
											$.ajax({
												url : "activeconsumers/"
														+ choosedTopic,
												type : "GET",
												success : function(groups) {
													generateGroupSelect(
															choosedTopic,
															groups);
												}
											});
											$('#create-consumer')
													.empty()
													.append(
															'<div class="cssload-container"><div class="cssload-loading"><i></i><i></i></div></div>');
										});
						$('#create-consumer')
								.empty()
								.append(
										'<select data-placeholder="Choose a Consumer..." class="chosen-select chosen-select-group" tabindex="2" form="taskForm" name="group" id="inputGroupName"><option value=""></option></select>');
						var select = $('#taskForm .chosen-select-group');
						select.find('option').remove().end().append(
								'<option value=""></option>');
						$('.chosen-select-group').chosen({
							width : "100%",
							no_results_text : 'Oops, no such Group!'
						})
					});

	$("#submitTask")
			.click(
					function() {
						var frm = $('#taskForm');
						var go = true;
						var inputThreshold = $("#inputThreshold").val();
						if (!inputThreshold || inputThreshold === "") {
							document.getElementById("inputThreshold").style.borderColor = "red";
							go = false;
						}
						var inputDiapause = $("#inputDiapause").val();
						if (!inputDiapause || inputDiapause === "") {
							document.getElementById("inputDiapause").style.borderColor = "red";
							go = false;
						}
						var inputEmail = $("#inputEmail").val();
						if (!inputEmail || inputEmail === "") {
							document.getElementById("inputEmail").style.borderColor = "red";
							go = false;
						}
						if (!go) {
							go = true;
							return;
						}

						var sendData = formArrToObject(frm.serializeArray());
						var choosedTopic = $(
								'#inputTopicName_chosen .chosen-single span')
								.text();
						sendData.topic = choosedTopic;
						$.ajax({
							beforeSend : function(xhrObj) {
								xhrObj.setRequestHeader("Content-Type",
										"application/json");
								xhrObj.setRequestHeader("Accept",
										"application/json");
							},
							type : frm.attr('method'),
							url : frm.attr('action'),
							dataType : "json",
							data : JSON.stringify(sendData),
							success : function(response) {
								$('#taskModal').modal('hide');
								swal({
									title : "Task created!",
									type : "success",
									timer : 1000,
									showConfirmButton : false
								});
								window.location.reload();
								// reloadcb(response)
							}
						});
					});
}

function alertTaskDetail(cb) {
	$('#taskDetailModal')
			.on(
					'show.bs.modal',
					function(event) {
						var button = $(event.relatedTarget);
						var group = button.data('group');
						var topic = button.data('topic');
						var threshold = button.data('threshold');
						var diapause = button.data('diapause');
						var mailTo = button.data('mailto');
						document.getElementById("taskDetail-inputThreshold").style.borderColor = "";
						document.getElementById("taskDetail-inputDiapause").style.borderColor = "";
						document.getElementById("taskDetail-inputEmail").style.borderColor = "";
						var modal = $(this);
						modal.find('.modal-body #taskDetail-inputTopic').val(
								topic);
						modal.find('.modal-body #taskDetail-inputConsumer')
								.val(group);
						$('#taskDetail-inputTopic').prop('readonly', true);
						$('#taskDetail-inputConsumer').prop('readonly', true);
						// Message Lag Threshold
						modal.find('.modal-body #taskDetail-inputThreshold')
								.val(threshold);
						// diapause
						modal.find('.modal-body #taskDetail-inputDiapause')
								.val(diapause);
						// Mail to
						modal.find('.modal-body #taskDetail-inputEmail').val(
								mailTo);
					});

	$("#updateTask")
			.click(
					function() {
						var frm = $('#taskDetailForm');
						var inputThreshold = $("#taskDetail-inputThreshold")
								.val();
						if (!inputThreshold || inputThreshold === "") {
							document
									.getElementById("taskDetail-inputThreshold").style.borderColor = "red";
							return;
						}
						var inputDiapause = $("#taskDetail-inputDiapause")
								.val();
						if (!inputDiapause || inputDiapause === "") {
							document.getElementById("taskDetail-inputDiapause").style.borderColor = "red";
							return;
						}
						var inputEmail = $("#taskDetail-inputEmail").val();
						if (!inputEmail || inputEmail === "") {
							document.getElementById("taskDetail-inputEmail").style.borderColor = "red";
							return;
						}

						var sendData = formArrToObject(frm.serializeArray());
						$.ajax({
							beforeSend : function(xhrObj) {
								xhrObj.setRequestHeader("Content-Type",
										"application/json");
								xhrObj.setRequestHeader("Accept",
										"application/json");
							},
							type : 'POST',
							url : frm.attr('action'),
							data : JSON.stringify(sendData),
							success : function(response) {
								$('#taskDetailModal').modal('hide');
								swal({
									title : "Task updated!",
									type : "success",
									timer : 1000,
									showConfirmButton : false
								});
								cb(response);
							}
						});
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