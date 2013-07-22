//Filename: JobPage.js

define([
    'jquery',
    'underscore',
    'logging',
    'vm',
    'util/dialogs',
    'constants/RDFNS',
    'BaseView',
    'models/workflow/WorkflowModel',
    'text!templates/job/workflowJobPageTemplate.html'
], function($,
    _,
    logging,
    Vm,
    dialogs,
    RDFNS,
    BaseView,
    WorkflowModel,
    workflowJobTemplate
    ) {

    var log = logging.getLogger("views/job/JobPage");

    return BaseView.extend({

        template : workflowJobTemplate,

        events : {
            "click #job-refresh" : function() {
                this.refreshJob();
                this.refreshLog();
                this.render();
            }
        },

        initialize : function(options) {
            this.workflow = WorkflowModel.findOrCreate(
                this.model.getQN("omnom:workflow")
            );
            this.workflow.url = this.model.getQN("omnom:workflow").id;
            this.workflow.fetch({async:true});
            console.log(this.workflow);
            this.listenTo(this.model, "change", this.render);
            this.jobLog = "";
            this.refreshLog();
        },

        renderProgressBar : function() {
            var progress = 100 *
                ( this.model.getQN("omnom:finishedPosition").length
                    / this.workflow.getQN("omnom:workflowPosition").length );
            this.$("div.progress div.bar").css("width", progress + "%");
        },

        refreshJob : function() {
            this.model.fetch();
        },

        refreshLog : function() {
            var that = this;
            $.ajax({
                url : this.model.id + "/log",
                type : "GET",
                headers : {
                    Accept : "text/x-log"
                },
                complete : function(jqXHR) {
                    if (jqXHR > 200) {
                        dialogs.notify("Could not retrieve log messages.", 'error');
                    } else {
                        that.jobLog = jqXHR.responseText;
                        that.model.trigger("change");
                    }
                }
            });
        },

        renderLog: function () {
            this.$("pre#job-log").html(this.jobLog);
        },
        render : function() {
            this.renderModel({ workflow: this.workflow.toJSON() });
            this.renderLog();
            this.renderProgressBar();
        }

    });
});
