var margin = {
	top : 20,
	right : 120,
	bottom : 20,
	left : 120
}, width = 960 - margin.right - margin.left, height = 800 - margin.top
		- margin.bottom;

var i = 0, duration = 750, root;

var tree = d3.layout.tree().size([ height, width ]);

var diagonal = d3.svg.diagonal().projection(function(d) {
	return [ d.y, d.x ];
});

var svg;

Highcharts.setOptions({
    global: {
        useUTC: false
    }
});


function loadViz(load_to_id, data_path) {
	svg = d3.select(load_to_id).append("svg").attr("width",
			width + margin.right + margin.left).attr("height",
			height + margin.top + margin.bottom).attr("style",
			"font-size:12px;color:#fff").append("g").attr("transform",
			"translate(" + margin.left + "," + margin.top + ")");
	d3.json(data_path, function(error, topic_group_map) {

		root = topic_group_map;
		root.x0 = height / 2;
		root.y0 = 0;

		function collapse(d) {
			if (d.children) {
				d._children = d.children;
				d._children.forEach(collapse);
				d.children = null;
			}
		}

		root.children.forEach(collapse);
		update(root);
		$(".alert-info").hide("slow");
	});
	d3.select(self.frameElement).style("height", "800px");
}

function intervalHighchart(esUrl) {
	setTimeout(intervalHighchart, 60000);
	var postBody = {
			"size": 2000,
		    "sort": [
		        { "timestamp": {"order" : "desc"}},
		        "broker"
		    ],
		    "query": {
		    	"bool": { 
		    	      "must": [
		    	    	  {"match": { "metric": "MessagesInPerSec" }}  
		    	      ],
		    	      "filter": [ 
		    	    	  {"range": {
		  		            "timestamp": {
		  		                "gte": new Date().getTime() - 480*60000,
		  		                "lte": new Date().getTime()
		  		            }
		  		        }} 
		    	      ]
		    	    }
		    }
		};
	
	$.post('http://'+ esUrl +'/jmxMetrics/_search', JSON.stringify(postBody), function(data) {
		var seriesOptions = [];
    	var hits = data.hits.hits;
    	let brokerHitsMap = new Map();
    	$.each(hits, function (i, hit){
    		var source = hit._source;
    		var brokerHits = brokerHitsMap.get(source.broker);
    		if(brokerHits == undefined){
    			brokerHitsMap.set(source.broker, [[source.timestamp, source.count]]);
    		}else{
    			brokerHits.push([source.timestamp, source.count]);
    		}
    	});
    	let i=0
    	for (let [broker, hits] of brokerHitsMap) {
    		hits.sort(function(a, b) {
    			return a[0] - b[0];
    		});
    		seriesOptions[i] = {
				name: broker,
				data: hits
    		};
    		i++;
    	}
        createChart(seriesOptions);
	});
}

/**
 * Create the chart when all data is loaded
 * 
 * @returns {undefined}
 */
function createChart(_seriesOptions) {

    Highcharts.stockChart('metrics', {
    	title: {
    		text: 'Message count (last 8 hours)'
    	},

        rangeSelector: {
            selected: 4,
            enabled: false
        },

        yAxis: {
            labels: {
                formatter: function () {
                    return this.value + '%';
                }
            },
            plotLines: [{
                value: 0,
                width: 2,
                color: 'silver'
            }]
        },

        plotOptions: {
            series: {
            	compare: 'percent',
            	showInNavigator: true
            }
        },

        tooltip: {
            pointFormat: '<span style="color:{series.color}">{series.name}</span>: <b>{point.y}</b> ({point.change}%)<br/>'
        },

        series: _seriesOptions
    });
}

function update(source) {

	// Compute the new tree layout.
	var nodes = tree.nodes(root).reverse(), links = tree.links(nodes);

	// Normalize for fixed-depth.
	nodes.forEach(function(d) {
		d.y = d.depth * 330;
	});

	// Update the nodes…
	var node = svg.selectAll("g.node").data(nodes, function(d) {
		return d.id || (d.id = ++i);
	});

	// Enter any new nodes at the parent's previous position.
	var nodeEnter = node.enter().append("g").attr("class", "node").attr(
			"transform", function(d) {
				return "translate(" + source.y0 + "," + source.x0 + ")";
			}).on("click", click);

	nodeEnter.append("circle").attr("r", 1e-6).style("fill", function(d) {
		return d._children ? "lightsteelblue" : "#333";
	});

	nodeEnter.append("text").attr("x", function(d) {
		if (isLastNode(d)) {
			return 10;
		} else {
			return d.children || d._children ? -10 : 10;
		}
	}).attr("dy", ".35em").attr("text-anchor", function(d) {
		if (isLastNode(d)) {
			return "start";
		} else {
			return d.children || d._children ? "end" : "start";
		}
	}).text(function(d) {
		return d.name;
	}).style("fill-opacity", 1e-6);// .style("fill", "#fff");

	// Transition nodes to their new position.
	var nodeUpdate = node.transition().duration(duration).attr("transform",
			function(d) {
				return "translate(" + d.y + "," + d.x + ")";
			});

	nodeUpdate.select("circle").attr("r", 4.5).style("fill", function(d) {
		if (isLastNode(d)) {
			return "#66CC00";
		} else {
			return d._children ? "lightsteelblue" : "#66CC00";
		}
	});

	nodeUpdate.select("text").style("fill-opacity", 1);

	// Transition exiting nodes to the parent's new position.
	var nodeExit = node.exit().transition().duration(duration).attr(
			"transform", function(d) {
				return "translate(" + source.y + "," + source.x + ")";
			}).remove();

	nodeExit.select("circle").attr("r", 1e-6);

	nodeExit.select("text").style("fill-opacity", 1e-6);

	// Update the links…
	var link = svg.selectAll("path.link").data(links, function(d) {
		return d.target.id;
	});

	// Enter any new links at the parent's previous position.
	link.enter().insert("path", "g").attr("class", "link").attr("d",
			function(d) {
				var o = {
					x : source.x0,
					y : source.y0
				};
				return diagonal({
					source : o,
					target : o
				});
			});

	// Transition links to their new position.
	link.transition().duration(duration).attr("d", diagonal);

	// Transition exiting nodes to the parent's new position.
	link.exit().transition().duration(duration).attr("d", function(d) {
		var o = {
			x : source.x,
			y : source.y
		};
		return diagonal({
			source : o,
			target : o
		});
	}).remove();

	// Stash the old positions for transition.
	nodes.forEach(function(d) {
		d.x0 = d.x;
		d.y0 = d.y;
	});
}

// Toggle children on click.
function click(d) {
	if (d.children) {
		d._children = d.children;
		d.children = null;
	} else {
		d.children = d._children;
		d._children = null;
	}
	if (d._children === null
			&& (d.children === null || (d.children != null && d.children.length == 0))) {
		return load_lag_page(d);
	}
	update(d);
}

function load_lag_page(d) {
	name = d.name
	if (d.parent != undefined) {
		parent = d.parent.name;
		if (parent != undefined && parent != "ActiveTopics"
				&& parent != "KafkaCluster") {
			window.location.replace("./#/group/" + name + "/" + parent);
		}
		if (parent != undefined && parent != "ActiveTopics"
				&& parent == "KafkaCluster") {
			window.location.replace("./#/broker/" + name);
		}
	}
}

function isLastNode(d) {
	if ((d.children == null)
			&& (d._children == null || (d._children != null && d._children.length == 0))) {
		return true;
	} else {
		return false;
	}
}