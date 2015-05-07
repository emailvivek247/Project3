$(document).ready(function(){
  $('.closing').each(function(){
    var all = $(this);
    var t = $('h2',this);
    var content = $(' h2 ~ *',all);
    t.replaceWith("<legend>" + t.text() + "</legend>");
    all.replaceWith("<fieldset style='"
      + (all.attr('style') ? all.attr('style') : 'width:90%; margin-left:auto; margin-right:auto;')
      + "'><legend><b>"+t.text()+"</b></legend><div class='form-item'>"
      + content.html() + "</div></fieldset>");
  });
});
