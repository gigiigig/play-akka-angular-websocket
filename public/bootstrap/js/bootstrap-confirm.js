(function ($) {
    $.fn.extend({
        //pass the options variable to the function
        confirmModal: function (options) {
            var html = 
            '<div class="modal fade" id="confirmContainer">' + 
            '  <div class="modal-dialog">' + 
            '    <div class="modal-content">' + 
            '      <div class="modal-header">' + 
            '        <a class="close" data-dismiss="modal">×</a>' +
            '        <h4>#Heading#</h4>' + 
            '      </div>' + 
            '      <div class="modal-body">' +
            '          #Body#' + 
            '      </div>' +
            '      <div class="modal-footer">' +
            '        <a href="#" class="btn btn-primary" id="confirmYesBtn">Confirm</a>' +
            '        <a href="#" class="btn btn-default" data-dismiss="modal">Cancel</a>' +
            '      </div>' +
            '    </div>' +
            '  </div>' +
            '</div>';

            var defaults = {
                heading: 'Please confirm',
                body:'Body contents',
                callback : null
            };
            
            var options = $.extend(defaults, options);
            html = html.replace('#Heading#',options.heading).replace('#Body#',options.body);
            $(this).html(html);
            var context = $("#confirmContainer"); 
            context.modal('show');            
            $('#confirmYesBtn',this).click(function(){
                if(options.callback!=null)
                    options.callback();
                $(context).modal('hide');
                return false;
            });
        }
    });

})(jQuery);