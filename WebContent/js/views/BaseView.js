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

        /**
         * Appends the of a view to the element specified by selector
         *
         * @param subview The subview
         * @param selector
         *
         */
        appendHTML: function (subview, selector) {
            var targetElem = selector === 'undefined'
                ? this.$(this.listSelector)
                : _.isString(selector)
                    ? this.$(selector)
                    : selector;

            console.log("Adding subview to ", targetElem);

            targetElem.append(subview.render().$el);
        },

        /**
         * Renders a template, including some helper functions
         */
        createHTML: function (tpl, options) {

            if (!options) options = {};

            var that = this;
            return _.template(tpl, _.extend({
                /**
                 * A reference to the RDFNS.js module
                 * @see RDFS.js
                 */
                NS: RDFNS,
                /**
                 * Returns the element of an object denoted by a qname, resolved by RDFNS.getQName
                 */
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
                /**
                 * Returns the last segment of the argument interepreted as URL
                 * last_url_segment("http://foo/bar/quux") = "quux"
                 */
                last_url_segment: function (arg_url) {
                    if (!arg_url) return "BLANK";
                    return arg_url.replace(/.*\//, "");
                },
                /**
                 * Returns the path part of the argument interpreted as URL
                 * url_path("http://foo/bar/quux") = "/foo/bar/quux"
                 */
                url_path: function (arg_url) {
                    if (!arg_url) return "BLANK";
                    return arg_url.replace(/http:\/\/[^\/]*/, "");
                },
                /**
                 * Creates a unique ID by using UUID.js (to be used for generation of HTML identifiers and such)
                 */
                unique_id: function() { return UUID.v4() },
            }, options ));
        },

        /**
         * Default render implementation, delegates to renderModel()
         *
         * @returns The view for method chaining
         */
        render: function () {
            log.debug("render() in baseview called");
            this.renderModel();
            return this;
        },

        /**
         * Render a model view, by compiling this.template and appending it to this.$el.
         *
         * @param [options]  Additional options are passed to createHTML
         */
        renderModel: function (options) {
            log.debug("renderModel() in baseview called");
//			console.log("renderModel() in baseview called with template %o", this.template);
            if (!this.template) {
                throw "Cannot render without a template!";
            }
            this.$el.html(
                this.createHTML(this.template, _.extend({
                        model: this.model ? this.model.toJSON() : {},
                        rawModel: this.model ? this.model : {},
                    }, options)));
        },

        /**
         * Render a collection
         *
         * @param itemViewOptions
         * @param listSelector
         * @param ItemView
         */
        renderCollection: function (itemViewOptions, listSelector, ItemView) {
            if (typeof ItemView === 'undefined') {
                ItemView = this.itemView;
                if (typeof ItemView === 'undefined') {
                    console.error("Must pass arg or set this.itemView to call renderCollection");
                    console.error(this);
                    return;
                }
            }
            if (typeof listSelector === 'undefined') {
                listSelector = this.listSelector
                if (typeof listSelector === 'undefined') {
                    console.error("Must pass arg or set this.listSelector to call renderCollection");
                    console.error(this);
                    return;
                }

            }
            log.debug("renderCollection() in BaseView called (Selector: '" + listSelector + "')");
            Vm.cleanupSubViews(this);
            _.each(this.collection.models, function (model) {
                var subview = Vm.createSubView(this, ItemView,
                    _.extend({
                        FOO : "FEFNEWKJNFWJKNK",
                        model: model,
                    }, itemViewOptions));
                this.appendHTML(subview, listSelector);
            }, this);
        },

        setButtonLoading : function(selector) {
            this.$("span", selector).first().addClass("loading-indicator");
        },
        unsetButtonLoading : function(selector) {
            this.$("span", selector).first().removeClass("loading-indicator");
        },

    });

    return BaseView;

});