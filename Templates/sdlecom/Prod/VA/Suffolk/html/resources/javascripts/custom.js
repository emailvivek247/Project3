/*
	Easy plugin to get element index position
	Author: Peerapong Pulpipatnan
	http://themeforest.net/user/peerapong
*/

$.fn.getIndex = function(){
	var $p=$(this).parent().children();
    return $p.index(this);
}

$.fn.setNav = function(){
	jQuery('#main_menu li ul').css({display: 'none'});

	jQuery('#main_menu li').each(function()
	{	
		
		var $sublist = jQuery(this).find('ul:first');
		
		jQuery(this).hover(function()
		{	
			$sublist.stop().css({overflow:'hidden', height:'auto', display:'none'}).slideDown(200, function()
			{
				jQuery(this).css({overflow:'visible', height:'auto', display: 'block'});
			});	
		},
		function()
		{	
			$sublist.stop().hide(50, function()
			{	
				jQuery(this).css({overflow:'hidden', display:'none'});
			});
		});	
		
	});
	
	jQuery('#main_menu li ul li').each(function()
	{
		
		jQuery(this).hover(function()
		{	
			jQuery(this).find('a:first').addClass('hover');
		},
		function()
		{	
			jQuery(this).find('a:first').removeClass('hover');
		});	
		
	});
}

jQuery(function () {

	jQuery('#slider').anythingSlider({
	        easing: "easeInOutExpo",
	        autoPlay: true,
	        delay: parseInt($('#slider_timer').val() * 1000),
	        startStopped: false,
	        animationTime: 600,
	        hashTags: true,
	        buildNavigation: true,
			pauseOnHover: true,
			startText: "Go",
	        stopText: "Stop"
	    });
	    
});
	   

$(document).ready(function(){ 

	$(document).setNav();
	
	$('input[title!=""]').hint();
	
	$('.portfolio_vimeo').fancybox({ 
		padding: 10,
		overlayColor: '#000',
		transitionIn: 'elastic',
		transitionOut: 'elastic',
		overlayOpacity: .8
	});
	
	$('.portfolio_youtube').fancybox({ 
		padding: 10,
		overlayColor: '#000',
		transitionIn: 'elastic',
		transitionOut: 'elastic',
		overlayOpacity: .8
	});
	
	$('.portfolio_image').fancybox({ 
		padding: 10,
		overlayColor: '#000',
		transitionIn: 'elastic',
		transitionOut: 'elastic',
		overlayOpacity: .8
	});
	
	$('.img_frame').fancybox({ 
		padding: 10,
		overlayColor: '#000',
		transitionIn: 'elastic',
		transitionOut: 'elastic',
		overlayOpacity: .8
	});
	
	$('.narm_gallery a').fancybox({ 
		padding: 0,
		overlayColor: '#000', 
		transitionIn: 'elastic',
		transitionOut: 'elastic',
		overlayOpacity: .8
	});
	
	$('.gallery1_hover').hide();
	$('.two_third').hover(function(){  
 			$(this).find('.gallery1_hover').css({ 'opacity': 0.8 }).fadeIn(400);
 			
 			$(this).click(function(){
 				$(this).find('a').click();
 			});
 		}  
  		, function(){  
  		
  			$(this).find('.gallery1_hover').fadeOut();
  		}  
  		
	);
	
	$('.gallery2_hover').hide();
	$('.one_half .gallery_image').hover(function(){  
 			$(this).find('.gallery2_hover').css({ 'opacity': 0.8 }).fadeIn(400);
 			
 			$(this).click(function(){
 				$(this).find('a').click();
 			});
 		}  
  		, function(){  
  		
  			$(this).find('.gallery2_hover').fadeOut();
  		}  
  		
	);
	
	$('.gallery3_hover').hide();
	$('.one_third .gallery_image').hover(function(){  
 			$(this).find('.gallery3_hover').css({ 'opacity': 0.8 }).fadeIn(400);
 			
 			$(this).click(function(){
 				$(this).find('a').click();
 			});
 		}  
  		, function(){  
  		
  			$(this).find('.gallery3_hover').fadeOut();
  		}  
  		
	);
	
	$('.gallery4_hover').hide();
	$('.one_fourth .gallery_image').hover(function(){  
 			$(this).find('.gallery4_hover').css({ 'opacity': 0.8 }).fadeIn(400);
 			
 			$(this).click(function(){
 				$(this).find('a').click();
 			});
 		}  
  		, function(){  
  		
  			$(this).find('.gallery4_hover').fadeOut();
  		}  
  		
	);
	
	$.validator.setDefaults({
		submitHandler: function() { 
		    var actionUrl = $('#contact_form').attr('action');
		    
		    $.ajax({
  		    	type: 'POST',
  		    	url: actionUrl,
  		    	data: $('#contact_form').serialize(),
  		    	success: function(msg){
  		    		$('#contact_form').hide();
  		    		$('#reponse_msg').html(msg);
  		    	}
		    });
		    
		    return false;
		}
	});
		    
		
	$('#contact_form').validate({
		rules: {
		    your_name: "required",
		    email: {
		    	required: true,
		    	email: true
		    },
		    message: "required"
		},
		messages: {
		    your_name: "Please enter your name",
		    email: "Please enter a valid email address",
		    agree: "Please enter some message"
		}
	});	
	
	if(BrowserDetect.browser == 'Explorer' && BrowserDetect.version < 8)
	{
		var zIndexNumber = 1000;
		$('div').each(function() {
			$(this).css('zIndex', zIndexNumber);
			zIndexNumber -= 10;
		});

		$('#thumbNav').css('zIndex', 1000);
		$('#thumbLeftNav').css('zIndex', 1000);
		$('#thumbRightNav').css('zIndex', 1000);
		$('#fancybox-wrap').css('zIndex', 1001);
		$('#fancybox-overlay').css('zIndex', 1000);
	}
	
	$(".accordion").accordion({ collapsible: true });
	
	$(".accordion_close").find('.ui-accordion-header a').click();
	
	$(".tabs").tabs();
	
	
	var photoItems = $('#content_wrapper .inner_slide .card').length;
	var photoWidth = parseInt($('#portfolio_width').val())+30;
	var scrollArea = photoWidth * photoItems;
	var scrollWidth = 930;
	
	$('#content_wrapper .inner_slide').css({width: scrollWidth+'px'});
	
	$("#content_wrapper .inner_slide .inner_wrapper").css('width', scrollArea);
	$("#content_wrapper .inner_slide").attr({scrollLeft: 0});					   
	
	$("#content_wrapper .inner_slide").css({"overflow":"hidden"});
	
	var auto_scroll = $('#nm_portfolio_auto_scroll').val();
	
	if(auto_scroll != 0)
	{
		$("#move_next").mouseenter( 
		
    		function() {
    	    	timerId = setInterval(function() { 
    	    	
    	    		var speed = parseInt($('#slider_speed').val());
					var slider = $('#content_slider');
					var sliderCurrent = slider.slider("option", "value");
					sliderCurrent += speed; // += and -= directions of scroling with MouseWheel
					
					if (sliderCurrent > slider.slider("option", "max")) sliderCurrent = slider.slider("option", "max");
					else if (sliderCurrent < slider.slider("option", "min")) sliderCurrent = slider.slider("option", "min");
					
					slider.slider("value", sliderCurrent);
    	    	
    	    	}, 100);
    	    	
    	    	//$(this).find('img').animate({ opacity: 1 }, 300);
    		}
    	);
    	$("#move_next").mouseleave( 
    		function() { 
    			clearInterval(timerId); 
    		}
		);
		
		$("#move_prev").mouseenter(
    		function() {
    	    	timerId = setInterval(function() { 
    	    	
    	    		var speed = parseInt($('#slider_speed').val());
					var slider = $('#content_slider');
					var sliderCurrent = slider.slider("option", "value");
					sliderCurrent -= speed; // += and -= directions of scroling with MouseWheel
					
					if (sliderCurrent > slider.slider("option", "max")) sliderCurrent = slider.slider("option", "max");
					else if (sliderCurrent < slider.slider("option", "min")) sliderCurrent = slider.slider("option", "min");
					
					slider.slider("value", sliderCurrent);
    	    	
    	    	}, 100);
    	    	
    	    	//$(this).find('img').animate({ opacity: 1 }, 300);
    		}
    	);
    	$("#move_prev").mouseleave(
    		function() { 
    			clearInterval(timerId); 
    		}
		);
	}
	
	$('#content_slider').slider({
		animate: 'slow',
		change: changeSlide,
		slide: doSlide
	});
	
	function changeSlide(e, ui)
	{
		var maxScroll = $("#content_wrapper .inner_slide").attr("scrollWidth") - $("#content_wrapper .inner_slide").width();
		var currentScroll = (ui.value * (maxScroll / 100))-65;
		$("#content_wrapper .inner_slide").stop().animate({scrollLeft: currentScroll}, 1200);
	}

	function doSlide(e, ui)
	{
		var maxScroll = $("#content_wrapper .inner_slide").attr("scrollWidth") - $("#content_wrapper .inner_slide").width();
		var currentScroll = (ui.value * (maxScroll / 100))-65;
		$("#content_wrapper .inner_slide").stop().attr({scrollLeft: currentScroll});
	}
	
	if($('#inner_slide').length > 0)
	{
	var position = $('#inner_slide').offset();

	$('#move_prev').css({ 'left': parseInt(position.left), 'top': parseInt(position.top) });
	$('#move_next').css({ 'left': parseInt(position.left+scrollWidth-100), 'top': parseInt(position.top) });
	
	$(window).resize(function() {
  		var position = $('#inner_slide').offset();
  		$('#move_prev').css({ 'left': parseInt(position.left), 'top': parseInt(position.top) });
		$('#move_next').css({ 'left': parseInt(position.left+scrollWidth-100), 'top': parseInt(position.top) });
	});
	}
	
	$('body').css({'visibility': 'visible'});
	
	$('#pp_narm_skins').change(function(){ 
 		$("link.skins").attr("href", 'css/skins/'+$(this).val()+'.css');
 		
 		$.ajax({
  				type: 'GET',
  				url: $('#form_option').attr('action'),
  				data: 'pp_narm_skins='+$(this).val()
			});
			
		Cufon.refresh();
	});
	
	$('#pp_narm_colors').change(function(){ 
 		$.ajax({
  				type: 'GET',
  				url: $('#form_option').attr('action'),
  				data: 'pp_narm_colors='+$(this).val()
			});
			
		location.href= 'index.html?pp_narm_colors='+$(this).val();
	});
	
	$('#theme_option_btn').click(function(){
 		$('#theme_option').slideToggle('fast', function() {
			$.ajax({
  				type: 'GET',
  				url: $('#form_option').attr('action'),
  				data: 'pp_show_hide_option='+$('#theme_option').css('display')
			});
 		}); 
 	});

});