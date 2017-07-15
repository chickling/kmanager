'use strict';

angular.module("offsetapp.directives", [])
	.directive('moment', function($timeout) {
		return {
			restrict: 'E',
			template: '<span>{{moment}}</span>',
			replace: true,
			scope: {
				timestamp: "@"
			},
			link: function(scope, element, attrs) {
				scope.$watch("timestamp", function(ov, nv) {
					var m = moment(parseInt(scope.timestamp));
					scope.moment = m.fromNow();
					$timeout(function() {
						scope.moment = m.fromNow();
					}, 5000);
				});
			}
		};
	})
	.directive('chart', function($timeout) {

		function findY(data, x, min) {
			for(var i=0; i< data.length; i++) {
				if(data[i][0] > x) {
					if(min) return data[i][1];
					else return data[Math.max(0, i-1)][1];
				}
			}
			return data[data.length-1][1];
		}

		function showSpeed(scope, min, max) {
			$timeout(function(){
				var duration = moment.duration(max.timestamp-min.timestamp);
				var deltaT_sec = (max.timestamp-min.timestamp)/1000;
				scope.deltaT_str = duration.humanize();
				var deltaIn = max.logSize-min.logSize;
				scope.inspeed = deltaIn/deltaT_sec;
				var deltaOut = max.offset-min.offset;
				scope.outspeed = deltaOut/deltaT_sec;
			});
		}

		return {
			restrict: 'E',
			template: '<div>'
				+ '<div class="row speed" ng-hide="loading">'
				+ '<div class="label label-info col-md-3">'
				+ '<span class="glyphicon glyphicon-log-in"></span> '
				+ '<strong>{{inspeed|number:1}}</strong> <span class="small">msg/s</span>'
				+ '<br><small>over {{deltaT_str}}.</small>'
				+ '</div>'
				+ '<div class="label label-info  col-md-3 col-md-offset-5">'
				+ '<strong>{{outspeed|number:1}}</strong> <span class="small">msg/s</span>'
				+ '<span class="glyphicon glyphicon-log-out"></span> '
				+ '<br><small>over {{deltaT_str}}.</small>'
				+ '</div>'
				+ '</div>'
				+ '<div class="row">'
				+ '<div class="chart ng-md-12" ng-hide="loading"></div>'
				+ '<div class="alert ng-md-12 alert-info" ng-show="loading">Loading</div>'
				+ '</div>'
				+ '</div>',
			replace: true,
			scope: {
				data: "="
			},

			link: function (scope, element, attrs) {
				var chart = undefined;

				function setupChart(data, element) {
					var d = _(data).map(function(p) {
						return [
							[p.timestamp, p.logSize],
							[p.timestamp, p.offset],
							[p.timestamp, p.logSize-p.offset]
						];
					}).unzip().value();
					scope.loading = data.length <= 0;
					if(data.length > 0) {
						showSpeed(scope, data[0], data[data.length-1]);
					}

					Highcharts.setOptions({
						global : {
							useUTC : false
						}
					});

					// Create the chart
					chart= new Highcharts.StockChart( {
						chart : {
							//backgroundColor: "#2E3338",
							//plotBackgroundColor:"#3E444C",
							height: 700,
							width:1200,
							renderTo: $(element).find(".chart")[0]
						},
						rangeSelector: {
							enabled: false,
							/*inputEnabled: false,
							buttons: [{
								type: 'minute',
								count: 5,
								text: '5m'
							}, {
								type: 'minute',
								count: 15,
								text: '15m'
							}, {
								type: 'hour',
								count: 1,
								text: '1h'
							}, {
								type: 'hour',
								count: 12,
								text: '12h'
							}, {
								type: 'day',
								count: 1,
								text: '1d'
							}, {
								type: 'day',
								count: 5,
								text: '5d'
							}, {
								type: 'day',
								count: 7,
								text: '7d'
							}]*/
						},
						legend : {
							borderRadius: 0,
							//backgroundColor:"#3E444C",
							borderColor: 'silver',
							enabled: true,
							margin: 30,
							itemMarginTop: 2,
							itemMarginBottom: 2,
							itemWidth:300,
							
							itemHiddenStyle: {
								color: "#2E3338"
							},
							itemStyle: {
								width:280,
								color: "#CCC"
							}
						},
						//				yAxis: axis,
						xAxis: {
							type: 'datetime',
							dateTimeLabelFormats: { // don't display the dummy year
								month: '%e. %b',
								year: '%b'
							},
							ordinal: false,
							events: {
								setExtremes: function(event) {
									var min = {
										timestamp: Math.floor(event.min),
										offset: findY(d[1], event.min, false),
										logSize: findY(d[0], event.min, false)
									};
									var max = {
										timestamp: Math.ceil(event.max),
										offset: findY(d[1], event.max, false),
										logSize: findY(d[0], event.max, false)
									};
									showSpeed(scope, min, max);
								}
							}
						},
						yAxis: [{
							title: {
								text: "Offset Position",
								style: {
									color: '#4572A7'
								}
							},
							labels: {
								style: {
									color: '#4572A7'
								}
							},
							opposite: false
						},{
							title: {
								text: "Lag",
								style: {
									color: '#EC4143'
								}
							},
							labels: {
								style: {
									color: '#EC4143'
								}
							},
							opposite: true
						}],
						series : [{
							name: "log size",
							data:d[0],
							yAxis: 0,
							color: '#088CFE',
							marker : {
								enabled : true,
								radius : 3
							}},
								  {
									  name: "offset",
									  data:d[1],
									  color: '#B9E6D9',
									  yAxis: 0,
									  marker : {
										  enabled : true,
										  radius : 3
									  }},
								  {
									  data:d[2],
									  name: "lag",
									  color: '#EC4143',
									  yAxis: 1,
									  marker : {
										  enabled : true,
										  radius : 3
									  }}]
					});
				}

				setupChart(scope.data, element);
				//Update when charts data changes
				scope.$watch("data.length", function(newValue, oldValue) {
					if(chart != undefined)  {
						chart.destroy();
						chart = undefined;
					}
					setupChart(scope.data, element);
				});
			}

		};
	});
