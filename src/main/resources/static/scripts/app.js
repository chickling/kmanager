var app = angular.module('kmanager',
	["kmanager.controllers", "kmanager.directives", "ngRoute"],
	function ($routeProvider) {
		$routeProvider
			.when("/", {
				templateUrl: "views/cluster-viz.html",
				controller: "ClusterVizCtrl"
			})
			.when("/groups", {
				templateUrl: "views/grouplist.html",
				controller: "GroupListCtrl"
			})
			.when("/group/:group", {
				templateUrl: "views/group.html",
				controller: "GroupCtrl"
			})
			.when("/group/:group/:topic", {
				templateUrl: "views/topic.html",
				controller: "TopicCtrl"
			})
			.when("/clusterviz", {
				templateUrl: "views/cluster-viz.html",
				controller: "ClusterVizCtrl"
			})
			.when("/activetopicsviz", {
				templateUrl: "views/activetopics-viz.html",
				controller: "ActiveTopicsVizCtrl"
			})
			.when("/topics", {
				templateUrl: "views/topiclist.html",
				controller: "TopicListCtrl"
			})
			.when("/topicdetail/:topic", {
				templateUrl: "views/topic-consumers.html",
				controller: "TopicConsumersCtrl"
			})
			.when("/alerts", {
				templateUrl: "views/alerts.html",
				controller: "AlertTaskListCtrl"
			})
			.when("/setting", {
				templateUrl: "views/setting.html",
				controller: "SettingCtrl"
			})
			.when("/broker/:endpoint", {
				templateUrl: "views/broker.html",
				controller: "BrokerCtrl"
			});
		;
	}).factory('isSystemReadyInterceptor', ["$location", function ($location) {
		var isSystemReadyInterceptor = {
			response: function (response) {
				if (response.data.isSystemReady != undefined && !response.data.isSystemReady) {
					$location.path("/setting");
				}
				return response;
			}
		}
		return isSystemReadyInterceptor;
	}]).config(['$httpProvider', function ($httpProvider) {
		$httpProvider.interceptors.push('isSystemReadyInterceptor');
	}]);

app.filter('size', function () {
	var toFixed = function(x){
		if(x<1){
			var res = x.toFixed(2);
			if(res == '0.00'){
				return '0';
			}
		}
		if(x<100){
			return x.toFixed(1);
		}
		return x.toFixed(0);
	};
	return function (x) {
		var val = parseFloat(x);

		if (val < 1) {
			return toFixed(val);
		}
		if(val<1000){
			return toFixed(val) + 'B'
		}
		val /= 1024;
		if (val < 10) {
			return toFixed(val) + 'K';
		}
		if (val < 1000) {
			return toFixed(val) + 'K';
		}
		val /= 1024;

		if (val < 10) {
			return toFixed(val) + 'M';
		}
		if (val < 1000) {
			return toFixed(val) + 'M';
		}
		val /= 1024;

		if (val < 10) {
			return toFixed(val) + 'G';
		}
		return toFixed(val) + 'G';
	};
})
angular.module("kmanager.services", ["ngResource"])
	.factory("offsetinfo", ["$resource", "$http", function ($resource, $http) {
		function processConsumer(cb) {
			return function (data) {
				data.offsets = groupPartitions(data.offsets);
				cb(data);
			}
		}

		function processMultipleConsumers(cb) {
			return function (data) {
				_(data.consumers.active).forEach(function (consumer) {
					consumer.offsets = groupPartitions(consumer.offsets);
				});
				_(data.consumers.inactive).forEach(function (consumer) {
					consumer.offsets = groupPartitions(consumer.offsets);
				});
				cb(data);
			};
		}

		function groupPartitions(data) {
			var groups = _(data).groupBy(function (p) {
				var t = p.timestamp;
				if (!t)
					t = 0;
				return p.group + p.topic + t.toString();
			});
			groups = groups.values().map(function (partitions) {
				return {
					group: partitions[0].group,
					topic: partitions[0].topic,
					partitions: partitions,
					logSize: _(partitions).pluck("logSize").reduce(function (sum, num) {
						return sum + num;
					}),
					offset: _(partitions).pluck("offset").reduce(function (sum, num) {
						return sum + num;
					}),
					timestamp: partitions[0].timestamp
				};
			}).value();
			return groups;
		}
		var apiHost = '';

		return {
			getGroup: function (group, cb) {
				return $resource(apiHost + "/group/:group").get({
					group: group
				}, processConsumer(cb));
			},
			topicDetail: function (topic, cb) {
				return $resource(apiHost + "/topicdetails/:topic").get({
					topic: topic
				}, cb);
			},
			topicConsumers: function (topic, cb) {
				return $resource(apiHost + "/topic/:topic/consumers").get({
					topic: topic
				}, processMultipleConsumers(cb));
			},
			loadTopicConsumerViz: function (group, cb) {
				cb(loadViz("#dataviz-container", apiHost + "/activetopics"))
			},
			listGroup: function () {
				return $http.get(apiHost + "/group");
			},
			cluster: function () {
				return $http.get(apiHost + "/cluster");
			},
			listTopics: function () {
				return $http.get(apiHost + "/topiclist");
			},
			getTopic: function (group, topic, cb) {
				return $resource(apiHost + "/group/:group/:topic").get({
					group: group,
					topic: topic
				}, processConsumer(cb));
			},
			newAlert: function (_url, requestBody, cb) {
				$http({
					method: 'POST',
					url: _url,
					headers: { 'Content-Type': 'application/json' },
					data: requestBody
				}).success(function (response) {
					cb(response);
				});
			},
			listTasks: function (cb) {
				return $http.get(apiHost + "/alerting/tasks");
			},
			deleteTask: function (task, cb) {
				return $http.delete(apiHost + "/alerting/delete/" + task.group + "-" + task.topic)
					.then(
					function (response) {
						cb(response);
					},
					function (response) {
						// failure call back
					}
					);
			},
			queryOffsetHistoryWithOptions: function (requestBody, cb) {
				$http({
					method: 'POST',
					url: apiHost + "/query",
					headers: { 'Content-Type': 'application/json' },
					data: requestBody
				}).success(function (response) {
					response.offsets = groupPartitions(response.offsets);
					cb(response);
				});
			},
			formatdate: function (date) {
				return date.getFullYear() + '-' + ('0' + (date.getMonth() + 1)).slice(-2) + '-' + ('0' + date.getDate()).slice(-2) + ' ' + ('0' + date.getHours()).slice(-2) + ':' + ('0' + date.getMinutes()).slice(-2);
			},
			getActiveGroups: function (topic) {
				return $http.get(apiHost + "/activeconsumers/" + topic);
			},
			isAlertEnabled: function () {
				return $http.get(apiHost + "/alerting/isAlertEnabled");
			},
			postSetting: function (requestBody, cb) {
				$http({
					method: 'POST',
					url: apiHost + "/setting",
					headers: { 'Content-Type': 'application/json' },
					data: requestBody
				}).success(function (response) {
					cb(response);
				});
			},
			getSetting: function () {
				return $http.get(apiHost + "/setting");
			},
			stats: function (bid) {
				var url = bid ? `/stats/broker/${bid}` : `/stats/brokers`;
				url = apiHost + url;
				return $http.get(url);
			},
			statsByTopic: function (topic) {
				return $http.get(apiHost + "/stats/topic/" + topic);
			}
		};
	}]);