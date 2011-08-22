/* Author:

 */

////////////////////
////// jQuery //////
////////////////////

$(document).ready(function(){

    // Top menu example
    $("body").bind("click", function(e) {
        $("ul.menu-dropdown").hide();
        $('a.menu').parent("li").removeClass("open").children("ul.menu-dropdown").hide();
    });
    $("a.menu").click(function(e) {
        var $target = $(this);
        var $parent = $target.parent("li");
        var $siblings = $target.siblings("ul.menu-dropdown");
        var $parentSiblings = $parent.siblings("li");
        if ($parent.hasClass("open")) {
            $parent.removeClass("open");
            $siblings.hide();
        } else {
            $parent.addClass("open");
            $siblings.show();
        }
        $parentSiblings.children("ul.menu-dropdown").hide();
        $parentSiblings.removeClass("open");
        return false;
    });

    // Tooltip example
    $("#ipsum").hover(
        function(event) {
            $("#ipsum-tip").show();
        },
        function(event) {
            $("#ipsum-tip").hide();
        }
    );


    Highcharts.setOptions({
        global: {
            useUTC: false
        }
    });


    var defaultData = function(min) {
        // generate an array of random data
        var data = [],
            time = (new Date()).getTime(),
            i;

        for (i = -19; i <= 0; i++) {
            data.push({
                x: time + i * 1000,
                y: Math.random() + (typeof min === 'undefined' ? 0 : min)
            });
        }
        return data;
    }

    var chart = new Highcharts.Chart({
        chart: {
            renderTo: 'chart',
            defaultSeriesType: 'spline',
            marginRight: 10,
            events: {
                load: function() {
                    var series = this.series;
                    var f = function() {
                        series[0].addPoint([(new Date()).getTime(), Math.random()], true, true);
                        series[1].addPoint([(new Date()).getTime(), Math.random() + 1], true, true);
                        series[2].addPoint([(new Date()).getTime(), Math.random() + 2], true, true);
                    }
                    setInterval(f, 2000);
                }
            }
        },
        title: {
            text: 'API Calls'
        },
        legend: {
            layout: 'horizontal',
            align: 'center',
            verticalAlign: 'bottom',
            borderWidth: 1
        },
        xAxis: {
            type: 'datetime',
            tickPixelInterval: 150
        },
        yAxis: {
            title: {
                text: 'calls/sec'
            },
            min: 0,
            max: 3

        },
        plotOptions: {
            spline: {
                lineWidth: 4,
                states: {
                    hover: {
                        lineWidth: 5
                    }
                },
                marker: {
                    enabled: false,
                    states: {
                        hover: {
                            enabled: true,
                            symbol: 'circle',
                            radius: 5,
                            lineWidth: 1
                        }
                    }
                },
                pointInterval: 3600000, // one hour
                pointStart: Date.UTC(2009, 9, 6, 0, 0, 0)
            }
        },
        tooltip: {
            formatter: function() {
                return ''+
                    Highcharts.dateFormat('%e. %b %Y, %H:00', this.x) +': '+ this.y +' m/s';
            }
        },
        series: [{
            name: 'Anonymous',
            data: defaultData()
        },{
            name: 'Authorized',
            data: defaultData(1)
        },{
            name: 'Admin',
            data: defaultData(2)
        }]
    });

});






