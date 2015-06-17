(function($) {

	$.fullCalendar.gcalFeed = function(feedUrl, options) {
		
		feedUrl = feedUrl.replace(/\/basic$/, '/full');
		options = options || {};
		
		return function(start, end, callback) {
			var params = {
				'start-min': $.fullCalendar.formatDate(start, 'u'),
				'start-max': $.fullCalendar.formatDate(end, 'u'),
				'singleevents': true,
				'max-results': 9999
			};
			var ctz = options.currentTimezone;
			if (ctz) {
				params.ctz = ctz = ctz.replace(' ', '_');
			}
			 $.getJSON(feedUrl, params, function(data) {
                                var events = [];
				if (data.items) {
					$.each(data.items, function(i,item){

						start = $.fullCalendar.parseISO8601(item.STARTDAYTIME , true),
						end = $.fullCalendar.parseISO8601(item.ENDDAYTIME , true),
						allDay = -1,
						url = "",
						events.push({
							id: item.COURT_SESSION_ID,
                                                        title: item.DISPLAYNAME + '(' + item.STARTDAYTIME + '-' + item.ENDDAYTIME + ')',
							url: url,
							start: start,
							end: end,
							allDay: allDay,
							location: "",
							description: "",
							className: options.className,
							editable: options.editable || false
						});
					});			                       
				}   
				callback(events);
			});
		}
	}

})(jQuery);
