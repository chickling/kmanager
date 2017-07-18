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
		} ])
	.controller("ClusterVizCtrl", [ "$scope", "$interval", "$routeParams", "offsetinfo",
		function($scope, $interval, $routeParams, offsetinfo) {
			$scope.loading = true;
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
			
			offsetinfo.isAlertEnabled().success(function(d) {
				if(!d.isAlertEnabled){
					$('#newTask').prop('disabled', true);
				}
			});
			
			offsetinfo.onShowNewAlertModal(true, function(d) {});
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
					offsetinfo.onShowAlertTaskDetailModal(function(d) {
						$scope.tasks = d.data;
						$scope.loading = false;
						location.reload();
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
					
					offsetinfo.onShowNewAlertModal(false, function(d) {
						// TODO reload task list here when new task add? 
					});
				}else{
					$scope.alertEnabled = false;
					$scope.loading = false;
					$('#newTask').prop('disabled', true);
				}
			});
		} ])
		.controller("SettingCtrl", [ "$scope", "offsetinfo",
		function($scope, offsetinfo) {
			var settingFormModal = {
					zkHosts: "",
					dataCollectFrequency: 1,
					excludeByLastSeen: 2592000,
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
				
				offsetinfo.postSetting($scope.settingForm, function(d) {
					if(d.isSystemReady!=undefined && d.isSystemReady){
						swal({
							title : "Setting updated! Kmonitor is ready to use!",
							type : "success",
							timer : 1000,
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
			}
		} ])
		/*.controller("BrokerCtrl", [ "$scope", "$routeParams", "offsetinfo",
		function($scope, $routeParams, offsetinfo) {
			$scope.loading = true;
			$scope.brokerEndpoint = $routeParams.endpoint;
			var options = {
				axisY : {
					type : Chartist.AutoScaleAxis,
					low : 4318293,
					high : 4319246,
					onlyInteger : true
				}
			};
			var data = {
				labels : [ '02:39:34', '02:40:04', '02:40:34', '02:41:04',
						'02:41:34', '02:42:04', '02:42:34', '02:43:04', '02:43:34',
						'02:44:04' ],
				series : [ [ 4318294, 4318393, 4318488, 4318603, 4318695, 4318808,
						4318922, 4319032, 4319146, 4319245 ] ]
			};
			new Chartist.Line('.ct-chart', data, options);
		} ])*/;