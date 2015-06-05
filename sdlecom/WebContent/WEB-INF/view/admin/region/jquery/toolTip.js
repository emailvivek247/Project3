$.fn.ToolTip = function(bgcolour, fgcolour)
{
  this.mouseover(
    function(e)
    {
      if((!this.title && !this.alt) && !this.tooltipset) return;
      // get mouse coordinates
      // based on code from http://www.quirksmode.org/js/events_properties.html
      var mouseX = e.pageX || (e.clientX ? e.clientX + document.body.scrollLeft : 0);
      var mouseY = e.pageY || (e.clientY ? e.clientY + document.body.scrollTop : 0);
      mouseX += 10;
      mouseY += 10;
      bgcolour = bgcolour || "#eee";
      fgcolour = fgcolour || "#000";
      // if there is no div containing the tooltip
      if(!this.tooltipdiv)
      {
        // create a div and style it
        var div = document.createElement("div");
        this.tooltipdiv = div;
        $(div).css(
        {
          border: "2px outset #ddd",
          padding: "2px",
          backgroundColor: bgcolour,
          color: fgcolour,
          position: "absolute"
        })
        // add the title/alt attribute to it
        .html((this.title || this.alt));
        this.title = "";
        this.alt = "";
        $("body").append(div);
        this.tooltipset = true;
      }
      $(this.tooltipdiv).show().css({left: mouseX + "px", top: mouseY + 3 + "px"});
    }
  ).mouseout(
    function()
    {
      if(this.tooltipdiv)
      {
        $(this.tooltipdiv).hide();
      }
    }
  );
  return this;
}

$(document).ready(
  function()
  {
    $(".tooltip").ToolTip("#fef", "#e00");
  }
);
