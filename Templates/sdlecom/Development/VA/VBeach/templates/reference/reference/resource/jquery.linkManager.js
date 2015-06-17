/*global window, jQuery */
/*jslint white: true, browser: true, onevar: true, undef: true, nomen: true, eqeqeq: true, bitwise: true, newcap: true, strict: true, maxerr: 50, indent: 4 */

/**
 * jQuery plugin for managing link targets.
 * 
 * Copyright (c) 2010 Ewen Elder
 *
 * Licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
 *
 * @author: Ewen Elder <glomainn at yahoo dot co dot uk> <ewen at jainaewen dot com>
 * @version: 1.0 (2010-07-30)
**/

'use strict';
(function ($)
{
	$.fn.linkManager = function (options)
	{
		var openThis, element, href, domain, pathname, querystring, hash;
		
		options = $.extend({}, $.linkManager.defaults, options);
		
		return $(this).each
		(
			function ()
			{
				$(this).find(options.selector).each
				(
					function ()
					{
						element = $(this);
						href = element.attr('href').replace('http://', '');
						
						
						// Get the domain, pathname and querystring.
						domain = href.indexOf('/') > -1 ? href.substr(0, href.indexOf('/')) : href;
						domain = domain.indexOf('?') > -1 ? domain.substr(0, href.indexOf('?')) : domain;
						domain = domain.indexOf('#') > -1 ? domain.substr(0, href.indexOf('#')) : domain;
						
						pathname = href.indexOf('/') > -1 ? href.substr(href.indexOf('/'), href.length) : '';
						pathname = pathname.indexOf('?') > -1 ? pathname.substr(0, pathname.indexOf('?')) : pathname;
						pathname = pathname.indexOf('#') > -1 ? pathname.substr(0, pathname.indexOf('#')) : pathname;
						
						querystring = href.indexOf('?') > -1 ? href.substr(href.indexOf('?') + 1) : '';
						querystring = querystring.indexOf('#') > -1 ? querystring.substr(0, querystring.indexOf('#')) : querystring;
						
						hash = href.indexOf('#') > -1 ? href.substr(href.indexOf('#') + 1) : '';
						
						
						// Check if link is external.
						openThis = domain !== window.location.hostname ? true : false;
						
						
						// Check for links to include.
						$(options.include).each
						(
							function ()
							{
								if (!openThis && (options.usePathname && pathname.indexOf(this) > -1) || (options.useQuerystring && querystring.indexOf(this) > -1) || (options.useHash && hash.indexOf(this) > -1))
								{
									openThis = true;
								}
							}
						);
						
						
						// Check for links to exclude.
						$(options.exclude).each
						(
							function ()
							{
								if ((options.useDomain && domain.indexOf(this) > -1) || (options.usePathname && pathname.indexOf(this) > -1) || (options.useQuerystring && querystring.indexOf(this) > -1) || (options.useHash && hash.indexOf(this) > -1))
								{
									openThis = false;
								}
							}
						);
						
						
						if (domain.length && openThis)
						{
							element.attr('target', options.target);
						}
					}
				);
			}
		);
	};
	
	
	$.linkManager = {
		version : 1.0,
		defaults : {
			selector : 'a:not([href=#])',
			target : '_blank',
			include : [],          // Internal links to include with the open new window rule.
			exclude : [],          // External links to exclude from the open new window rule.
			useDomain : true,      // Applies to external links only, set this to false to prevent the domain from being used in 'exclude' matches.
			usePathname : true,    // Applies to external links only, set this to false to prevent the pathname from being used in 'exclude' matches.
			useQuerystring : true, // Applies to both external and internal links, set this to false to prevent the querystring from being used in 'include' and 'exclude' matches.
			useHash : false        // Applies to both external and internal links, set this to true to search the hash section of the URL.
		}
	};
})(jQuery);
