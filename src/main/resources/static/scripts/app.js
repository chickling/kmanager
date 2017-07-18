var app = angular.module('offsetapp',
	[ "offsetapp.controllers", "offsetapp.directives", "ngRoute" ],
	function($routeProvider) {
		$routeProvider
			.when("/", {
				templateUrl : "views/cluster-viz.html",
				controller : "ClusterVizCtrl"
			})
			.when("/groups", {
				templateUrl : "views/grouplist.html",
				controller : "GroupListCtrl"
			})
			.when("/group/:group", {
				templateUrl : "views/group.html",
				controller : "GroupCtrl"
			})
			.when("/group/:group/:topic", {
				templateUrl : "views/topic.html",
				controller : "TopicCtrl"
			})
			.when("/clusterviz", {
				templateUrl : "views/cluster-viz.html",
				controller : "ClusterVizCtrl"
			})
			.when("/activetopicsviz", {
				templateUrl : "views/activetopics-viz.html",
				controller : "ActiveTopicsVizCtrl"
			})
			.when("/topics", {
				templateUrl : "views/topiclist.html",
				controller : "TopicListCtrl"
			})
			.when("/topicdetail/:topic", {
				templateUrl : "views/topic-detail.html",
				controller : "TopicDetailCtrl"
			})
			.when("/topic/:topic/consumers", {
				templateUrl : "views/topic-consumers.html",
				controller : "TopicConsumersCtrl"
			})
			.when("/alerts", {
				templateUrl : "views/alerts.html",
				controller : "AlertTaskListCtrl"
			})
			.when("/setting", {
				templateUrl : "views/setting.html",
				controller : "SettingCtrl"
			})
			/*
			 * .when("/broker/:endpoint", { templateUrl : "views/broker.html",
			 * controller : "BrokerCtrl" })
			 */;
		;
	}).factory('isSystemReadyInterceptor', ["$location", function($location) {
		var isSystemReadyInterceptor = {
			// request: function(config) {
			// },
			response: function(response) {
				if(response.data.isSystemReady!=undefined && !response.data.isSystemReady){
					$location.path("/setting");
				}
				return response;
			}
		}
		return isSystemReadyInterceptor;
	}]).config(['$httpProvider', function($httpProvider) {
		$httpProvider.interceptors.push('isSystemReadyInterceptor');
	}]);

angular.module("offsetapp.services", [ "ngResource" ])
	.factory("offsetinfo", [ "$resource", "$http", function($resource, $http) {
		function processConsumer(cb) {
			return function(data) {
				data.offsets = groupPartitions(data.offsets);
				cb(data);
			}
		}

		function processMultipleConsumers(cb) {
			return function(data) {
				_(data.consumers.active).forEach(function(consumer) {
					consumer.offsets = groupPartitions(consumer.offsets);
				});
				_(data.consumers.inactive).forEach(function(consumer) {
					consumer.offsets = groupPartitions(consumer.offsets);
				});
				cb(data);
			};
		}

		function groupPartitions(data) {
			var groups = _(data).groupBy(function(p) {
				var t = p.timestamp;
				if (!t)
					t = 0;
				return p.group + p.topic + t.toString();
			});
			groups = groups.values().map(function(partitions) {
				return {
					group : partitions[0].group,
					topic : partitions[0].topic,
					partitions : partitions,
					logSize : _(partitions).pluck("logSize").reduce(function(sum, num) {
						return sum + num;
					}),
					offset : _(partitions).pluck("offset").reduce(function(sum, num) {
						return sum + num;
					}),
					timestamp : partitions[0].timestamp
				};
			}).value();
			return groups;
		}

		return {
			getGroup : function(group, cb) {
				return $resource("./group/:group").get({
					group : group
				}, processConsumer(cb));
			},
			topicDetail : function(topic, cb) {
				return $resource("./topicdetails/:topic").get({
					topic : topic
				}, cb);
			},
			topicConsumers : function(topic, cb) {
				return $resource("./topic/:topic/consumers").get({
					topic : topic
				}, processMultipleConsumers(cb));
			},
			loadClusterViz : function(group, cb) {
				cb(loadViz("#dataviz-container", "./clusterlist"))
			},
			loadTopicConsumerViz : function(group, cb) {
				cb(loadViz("#dataviz-container", "./activetopics"))
			},
			listGroup : function() {
				return $http.get("./group");
			},
			listTopics : function() {
				return $http.get("./topiclist");
			},
			getTopic : function(group, topic, cb) {
				return $resource("./group/:group/:topic").get({
					group : group,
					topic : topic
				}, processConsumer(cb));
			},
			newAlert : function(_url, requestBody, cb) {
				$http({
				    method: 'POST',
				    url: _url,
				    headers: {'Content-Type': 'application/json'},
				    data: requestBody
				}).success(function (response) {
					cb(response);
				});	
			},
			listTasks : function(cb) {
				return $http.get("./alerting/tasks");
			},
			deleteTask: function(task, cb) {
				return $http.delete("./alerting/delete/" + task.group + "-" + task.topic)
				   .then(
					       function(response){
					    	   cb(response);
					       }, 
					       function(response){
					         // failure call back
					       }
					    );
			},
			queryOffsetHistoryWithOptions: function(requestBody, cb) {
				$http({
				    method: 'POST',
				    url: "./query",
				    headers: {'Content-Type': 'application/json'},
				    data: requestBody
				}).success(function (response) {
					response.offsets = groupPartitions(response.offsets);
					cb(response);
				});
			},
			formatdate: function(date) {
				return date.getFullYear() + '-' + ('0' + (date.getMonth() + 1)).slice(-2) + '-' + ('0' + date.getDate()).slice(-2) + ' ' + ('0' + date.getHours()).slice(-2) + ':' + ('0' + date.getMinutes()).slice(-2);
			},
			getActiveGroups: function(topic) {
				return $http.get("./activeconsumers/" + topic);
			},
			isAlertEnabled: function(){
				return $http.get("./alerting/isAlertEnabled");
			},
			postSetting: function(requestBody, cb) {
				$http({
				    method: 'POST',
				    url: "./setting",
				    headers: {'Content-Type': 'application/json'},
				    data: requestBody
				}).success(function (response) {
					cb(response);
				});
			},
			getSetting: function() {
				return $http.get("./setting");
			}
		};
	} ]);