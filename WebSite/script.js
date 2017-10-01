function auto() {
var input = $('#q').val();  
    $('#q').autocomplete({        
        source: function( request, response ) {
            $.ajax({                
                url: "http://localhost/auto.php?input=" + input,
                dataType: "json",
                data: {term: request.term},
                success: function(data) {
                    var arr = data["suggest"]["suggest"][input]["suggestions"];
                    response($.map(arr, function(item) {
                        return {
                            label:item["term"]                            
                            };
                    }));
                }
            });            
        },
        minLength: 1
    })    
}