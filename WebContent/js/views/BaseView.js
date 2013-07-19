define([ 'jquery', 'underscore', 'logging', 'backbone', 'vm', 'uuid', 'constants/RDFNS'
], function ($, _, logging, Backbone, Vm, UUID, RDFNS) {

    var log = logging.getLogger("views.BaseView");

    var BaseView = Backbone.View.extend({


        listSelector: 'div.list-container',

        /**
         * Renders subviews
         *
         * @see http://ianstormtaylor.com/rendering-views-in-backbonejs-isnt-always-simple/
         */
        assign: function (subview, selector, callback) {
            subview.setElement(this.$(selector)).render();
            if (typeof callback === 'function') {
                callback();
            }
        },

        appendHTML: function (subview, selector) {
            var targetElem = selector
                ? _.isString(selector)
                ? this.$(selector)
                : selector
                : this.$(this.listSelector);

            targetElem.append(subview.render().$el);
        },

        createHTML: function (tpl, options) {

            if (!options) options = {};

            var that = this;
            return _.template(tpl, _.extend(options, {
                NS: RDFNS,
                rdf_attr: function (qname, arg_model) {
                    var model = arg_model
                        ? arg_model
                        : options.model
                        ? options.model
                        : that.model
                        ? that.model
                        : {};
                    if (model.attributes)
                        model = model.attributes;
                    return RDFNS.rdf_attr(model, qname);
                },
                last_url_segment: function (arg_url) {
                    if (!arg_url) return "BLANK";
                    return arg_url.replace(/.*\//, "");
                },
                url_path: function (arg_url) {
                    if (!arg_url) return "BLANK";
                    return arg_url.replace(/http:\/\/[^\/]*/, "");
                },
                unique_id: function() { return UUID.v4() },
            }));
        },

        render: function () {
            log.debug("render() in baseview called");
            this.doRender();
            return this;
        },

        doRender: function (options) {
            log.debug("doRender() in baseview called");
//			console.log("doRender() in baseview called with template %o", this.template);
            if (!this.template) {
                throw "Cannot render without a template!";
            }
            this.$el.html(
                this.createHTML(this.template, _.extend({
                        model: this.model ? this.model.toJSON() : {},
                        rawModel: this.model ? this.model : {},
                    },
                    options
                )
                )
            );
        },

        renderCollection: function (ItemView, itemViewOptions, listSelector) {
            log.debug("renderCollection() in BaseView called (Selector: " + listSelector + ")");
            Vm.cleanupSubViews(this);
            _.each(this.collection.models, function (model) {
                var subview = Vm.createSubView(this, ItemView,
                    _.extend({
                        model: model,
                    }, itemViewOptions));
                this.appendHTML(subview, listSelector);
            }, this);
        }

    });

    return BaseView;

});