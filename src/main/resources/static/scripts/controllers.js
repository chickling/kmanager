angular.module('offsetapp.controllers', [ "offsetapp.services" ])
	.controller("GroupCtrl", [ "$scope", "$interval", "$routeParams", "offsetinfo",
		function($scope, $interval, $routeParams, offsetinfo) {
			offsetinfo.getGroup($routeParams.group, function(d) {
				$scope.info = d;
				$scope.loading = false;
			});
			$scope.loading = true;

			$scope.group = $routeParams.group;
			$scope.groupBy = 'consumer';
		} ])
	.controller("GroupListCtrl", [ "$scope", "offsetinfo",
		function($scope, offsetinfo) {
			$scope.loading = true;
			offsetinfo.listGroup().success(function(d) {
				$scope.loading = false;
				$scope.groups = d;
			});
		} ])
	.controller("TopicListCtrl", [ "$scope", "offsetinfo",
		function($scope, offsetinfo) {
			$scope.loading = true;
			offsetinfo.listTopics().success(function(d) {
				$scope.loading = false;
				$scope.topics = d;
			});
		} ])
	.controller("TopicDetailCtrl", [ "$scope", "$interval", "$routeParams", "offsetinfo",
		function($scope, $interval, $routeParams, offsetinfo) {
			offsetinfo.topicDetail($routeParams.topic, function(d) {
				$scope.info = d;
				$scope.loading = false;
			});
			$scope.loading = true;
			
			$scope.topic = $routeParams.topic;
			offsetinfo.brokerTopicMetricsForTopic($scope.topic).success(function(d) {
				if(d.BytesInPerSec){
					$scope.jmxEnabled = true;
				}
				$scope.brokerTopicMetrics = d;
			});
		} ])
	.controller("TopicConsumersCtrl", [ "$scope", "$interval", "$routeParams", "offsetinfo",
		function($scope, $interval, $routeParams, offsetinfo) {
			offsetinfo.topicConsumers($routeParams.topic, function(d) {
				$scope.info = d;
				$scope.loading = false;
			});
			$scope.loading = true;

			$scope.topic = $routeParams.topic;
			$scope.groupBy = 'topic';
			offsetinfo.brokerTopicMetricsForTopic($scope.topic).success(function(d) {
				if(d.BytesInPerSec){
					$scope.jmxEnabled = true;
				}
				$scope.brokerTopicMetrics = d;
			});
		} ])
	.controller("ClusterVizCtrl", [ "$scope", "$interval", "$routeParams", "offsetinfo",
		function($scope, $interval, $routeParams, offsetinfo) {
			$scope.loading = true;
			let esUrl;
			offsetinfo.brokerTopicMetricsForBrokers().success(function(d) {
				if(d.BytesInPerSec){
					$scope.jmxEnabled = true;
					esUrl = d.esUrl;
				}
				$scope.brokerTopicMetrics = d;
				offsetinfo.loadMessagesInChart(esUrl, function(d) {});
			});
			offsetinfo.loadClusterViz($routeParams.group, function(d) {});
		} ])
	.controller("ActiveTopicsVizCtrl", [ "$scope", "$interval", "$routeParams", "offsetinfo",
		function($scope, $interval, $routeParams, offsetinfo) {
			$scope.loading = true;
			offsetinfo.loadTopicConsumerViz($routeParams.group, function(d) {});
		} ])
	.controller("TopicCtrl", [ "$scope", "$routeParams", "offsetinfo",
		function($scope, $routeParams, offsetinfo) {
			var now = new Date();
			now.setSeconds(0);
			now.setMilliseconds(0);
			$scope.rangeform = {
			    group: $routeParams.group,
			    topic: $routeParams.topic,
			    range: "1h",
			    rangeto: now.getTime() + '',
			    interval: "1m",
			    rangetoStr: offsetinfo.formatdate(now)
			};
			$scope.rangeOptions = [
				{code: "1h", value: "last 1 hours"},
				{code: "8h", value: "last 8 hours"},
				{code: "16h", value: "last 16 hours"},
				{code: "1d", value: "last 1 day"},
				{code: "2d", value: "last 2 days"},
				{code: "1w", value: "last 1 week"}
			];
			
			$scope.intervalOptions = [
				{code: "1m", value: "1 minute"},
				{code: "10m", value: "10 minute"},
				{code: "30m", value: "30 minute"}
			];
			
			$scope.group = $routeParams.group;
			$scope.topic = $routeParams.topic;
			$scope.date = now;
			$scope.data = [];
			offsetinfo.getTopic($routeParams.group, $routeParams.topic, function(d) {
				$scope.data = d.offsets;
				$scope.loading = false;
				$('.form_datetime').datetimepicker({
					//language:  'fr',
					weekStart : 1,
					todayBtn : 0,
					autoclose : true,
					todayHighlight : 1,
					startView : 2,
					minView : 0,
					forceParse : true,
					showMeridian : 0,
					format : 'yyyy-mm-dd hh:ii',
					minuteStep : 1,
					endDate: now
				}).on('changeDate', function(ev){
					var date;
					if(ev.date == null){
						date = new Date();
						date.setSeconds(0);
						date.setMilliseconds(0);
						$scope.rangeform.rangeto = date.getTime() + '';
						$scope.rangeform.rangetoStr = offsetinfo.formatdate(date);
					}else{
					  	date = new Date(ev.date.valueOf());
					  	$scope.rangeform.rangeto = ev.date.valueOf() + '';
						$scope.rangeform.rangetoStr = offsetinfo.formatdate(date);	
					}
				});
			});
			
			
			$scope.task = {
				group : $routeParams.group,
				topic : $routeParams.topic,
				diapause : "",
				threshold : "",
				mailTo : ""
			}
			
			var isTaskExists = false;
			offsetinfo.isAlertEnabled().success(function(d) {
				if(!d.isAlertEnabled){
					$('#newTask').prop('disabled', true);
				}else{
					offsetinfo.listTasks().success(function(d) {
						d.forEach(function(t) {
							if (t.group === $routeParams.group && t.topic === $routeParams.topic) {
								isTaskExists = true;
								$scope.task = t;
							}
						});
					});
				}
			});
			
			$('#taskModal')
			.on(
					'show.bs.modal',
					function(event) {
						var button = $(event.relatedTarget);
						var group = $routeParams.group;
						var topic = $routeParams.topic;
						var modal = $(this);
						modal.find('.modal-body #topic').val(topic);
						modal.find('.modal-body #group').val(group);
						if(isTaskExists){
							modal.find('.modal-title').text('Task already exists!');
							modal.find('.modal-body #inputThreshold').val($scope.task.threshold);
							modal.find('.modal-body #inputDiapause').val($scope.task.diapause);
							modal.find('.modal-body #inputEmail').val($scope.task.mailTo);
							return;
						}
						document.getElementById("inputThreshold").style.borderColor = "";
						document.getElementById("inputEmail").style.borderColor = "";
						modal.find('.modal-title').text('New Task');
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
						if(inputThreshold === $scope.task.threshold+'' && inputDiapause === $scope.task.diapause+'' && inputEmail === $scope.task.mailTo){
							swal({
								  title: "Are you sure?",
								  text: "The task is already exists and you have do no change!",
								  type: "warning",
								  showCancelButton: true,
								  confirmButtonColor: "#DD6B55",
								  confirmButtonText: "Yes, still submit!",
								  cancelButtonText: "No, cancel",
								  closeOnConfirm: false,
								  closeOnCancel: true
								},
								function(isConfirm){
								  if (!isConfirm) {
								    return;
								  }
								  offsetinfo.newAlert('alerting/task', sendData, function(d) {
										$scope.tasks = d;
										$('#taskModal').modal('hide');
										swal({
											title : "Task created!",
											type : "success",
											timer : 1000,
											showConfirmButton : false
										});
									});
								});
						}else{
							offsetinfo.newAlert('alerting/task', sendData, function(d) {
								$scope.tasks = d;
								$('#taskModal').modal('hide');
								swal({
									title : "Task created!",
									type : "success",
									timer : 1000,
									showConfirmButton : false
								});
							});
						}
					});
			$scope.offsetHistoryByDateRange = function() {
				$scope.loading = true;
				offsetinfo.queryOffsetHistoryWithOptions(JSON.stringify($scope.rangeform), function(d) {
					$scope.data = d.offsets;
					$scope.loading = false;
				});
			}
			$scope.onRangeChanged = function() {
				if($scope.rangeform.range === "1h"){
					$scope.intervalOptions = [
						{code: "1m", value: "1 minute"},
						{code: "10m", value: "10 minute"},
						{code: "30m", value: "30 minute"}
					];
				}else if($scope.rangeform.range === "8h" 
					|| $scope.rangeform.range === "16h" 
					|| $scope.rangeform.range === "1d" 
					|| $scope.rangeform.range === "2d"){
					$scope.intervalOptions = [
						{code: "1m", value: "1 minute"},
						{code: "10m", value: "10 minute"},
						{code: "30m", value: "30 minute"},
						{code: "1h", value: "1 hour"}
					];
				}else{
					$scope.intervalOptions = [
						{code: "1m", value: "1 minute"},
						{code: "10m", value: "10 minute"},
						{code: "30m", value: "30 minute"},
						{code: "1h", value: "1 hour"},
						{code: "1d", value: "1 day"}
					];
				}
			}
		} ])
	.controller("AlertTaskListCtrl", [ "$scope", "offsetinfo",
		function($scope, offsetinfo) {
			$scope.loading = true;
			offsetinfo.isAlertEnabled().success(function(d) {
				if(d.isAlertEnabled){
					$scope.alertEnabled = true;
					offsetinfo.listTasks().success(function(d) {
						$scope.tasks = d;
						$scope.loading = false;
					});
					
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
								
								offsetinfo.newAlert('alerting/task', sendData, function(d) {
									$scope.tasks = d;
									$('#taskDetailModal').modal('hide');
									swal({
										title : "Task updated!",
										type : "success",
										timer : 1000,
										showConfirmButton : false
									});
								});
							});
					
					$scope.taskform = {
							group: "",
							topic: "",
							threshold: 1,
							diapause: 60,
							mailTo: ""
					}
					var topicOptions = new Array();
					offsetinfo.listTopics().success(function(d) {
						$scope.loading = false;
						for(var i=0;i<d.length; i++){
							topicOptions.push({code: d[i], value: d[i]});
						}
						$scope.topicOptions = topicOptions;
						$scope.taskform.topic = "";
					});
					$scope.deleteTask = function(t) {
						offsetinfo.deleteTask(t, function(d) {
							$scope.tasks = d.data;
							$scope.loading = false;
						});
					}
					
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
																	'<div id="escapingBallG"><div id="escapingBall_1" class="escapingBallG"></div></div>');
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
				}else{
					$scope.alertEnabled = false;
					$scope.loading = false;
					$('#newTask').prop('disabled', true);
				}
				
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
							
							offsetinfo.newAlert('alerting/task', sendData, function(d) {
								$scope.tasks = d;
								$('#taskModal').modal('hide');
								swal({
									title : "Task created!",
									type : "success",
									timer : 1000,
									showConfirmButton : false
								});
							});
						});
			});
		} ])
		.controller("SettingCtrl", [ "$scope", "offsetinfo",
		function($scope, offsetinfo) {
			var settingFormModal = {
					zkHosts: "",
					dataCollectFrequency: 1,
					excludeByLastSeen: 2592000,
					apiType: "",
					esHosts: "",
					esIndex: "",
					docTypeForOffset: "kafkaoffsetinfo",
					isAlertEnabled: false,
					smtpServer: "",
					smtpAuth: false,
					smtpUser: "",
					smtpPasswd: "",
					mailSender: "",
					mailSubject: ""
			}
			
			$scope.apiTypes = ["Java API", "REST API"];
			offsetinfo.getSetting().success(function(d) {
				if(d.isSystemReady!=undefined && !d.isSystemReady){
					$scope.settingForm = settingFormModal;
				}else{
					$scope.settingForm = d;
				}
			});
			$scope.submitSetting = function() {
				if(!$scope.settingForm.isAlertEnabled){
					$scope.settingForm.smtpServer = "";
					$scope.settingForm.smtpAuth = false;
					$scope.settingForm.smtpUser = "";
					$scope.settingForm.smtpPasswd = "";
					$scope.settingForm.mailSender = "";
					$scope.settingForm.mailSubject = "";
				}
				
				swal({
					  title: "Submit setting",
					  text: "Make sure you have input everything right then click OK",
					  type: "info",
					  showCancelButton: false,
					  closeOnConfirm: false,
					  showLoaderOnConfirm: true
					},
					function(){
						offsetinfo.postSetting($scope.settingForm, function(d) {
							if(d.isSystemReady!=undefined && d.isSystemReady){
								swal({
									title : "Setting updated! Kmonitor is ready to use!",
									type : "success",
									timer : 1500,
									showConfirmButton : false
								});
							}else{
								swal({
									title : "Something went wrong!",
									text: d.message,
									type : "error",
									showConfirmButton : true
								});
							}
						});
					});
			}
		} ])
		.controller("BrokerCtrl", [ "$scope", "$routeParams", "offsetinfo",
		function($scope, $routeParams, offsetinfo) {
			$scope.loading = true;
			$scope.brokerEndpoint = $routeParams.endpoint;
			offsetinfo.brokerTopicMetricsForBroker($scope.brokerEndpoint.split(":", 1)[0]).success(function(d) {
				if(d.BytesInPerSec){
					$scope.jmxEnabled = true;
				}
				$scope.brokerTopicMetrics = d;
			});
			
//			Morris.Donut({
//				  element: 'donut-physicalMemory',
//				  data: [
//				    {label: "FreePhysicalMemorySize", value: 12},
//				    {label: "UsedPhysicalMemorySize", value: 88}
//				  ]
//				});
//			var options = {
//				axisY : {
//					type : Chartist.AutoScaleAxis,
//					low : 4318293,
//					high : 4319246,
//					onlyInteger : true
//				}
//			};
//			var data = {
//				labels : [ '02:39:34', '02:40:04', '02:40:34', '02:41:04',
//						'02:41:34', '02:42:04', '02:42:34', '02:43:04', '02:43:34',
//						'02:44:04' ],
//				series : [ [ 4318294, 4318393, 4318488, 4318603, 4318695, 4318808,
//						4318922, 4319032, 4319146, 4319245 ] ]
//			};
//			new Chartist.Line('.ct-chart', data, options);
		} ]);