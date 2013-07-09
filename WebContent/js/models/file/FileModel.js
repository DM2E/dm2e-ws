define([
  'underscore',
  'backbone',
], function(_, Backbone) {
    
  var FileModel = Backbone.Model.extend({

      defaults : {
          originalName: "Untitled Document :)",
          fileType: "http://onto.dm2e.eu/omnom-types/Unknown",
      },

  });

  return FileModel;

});
