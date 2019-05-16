(ns wh.issue.views
  (:require
    #?(:cljs [reagent.core :as r])
    #?(:cljs [wh.components.ellipsis.views :refer [ellipsis]])
    #?(:cljs [wh.components.forms.views :refer [radio-buttons]])
    #?(:cljs [wh.components.overlay.views :refer [popup-wrapper]])
    [clojure.string :as str]
    [wh.components.common :refer [link img wrap-img]]
    [wh.components.icons :refer [icon]]
    [wh.components.issue :as issue :refer [issue-card level->str level->icon]]
    [wh.components.job :refer [job-card]]
    [wh.how-it-works.views :as how-it-works]
    [wh.issue.edit.views :as edit-issue]
    [wh.issue.events :as events]
    [wh.issue.subs :as subs]
    [wh.pages.util :as putil]
    [wh.re-frame.events :refer [dispatch]]
    [wh.re-frame.subs :refer [<sub]]
    [wh.util :as util]))

(defn show-popup-button []
  [:button.button
   {:on-click #(dispatch [::events/try-contribute])}
   (if (<sub [::subs/viewer-contributed?])
     "How to contribute"
     "Start work")])

(defn sidebox-row [icon-name content skeleton? & [class]]
  [:div {:class (util/merge-classes "issue__infobox-row" icon-name (when skeleton? "skeleton") class)}
   [icon (if skeleton? "circle" icon-name)]
   [:span (when-not skeleton? content)]])

(defn infobox []
  (let [skeleton? (nil? (<sub [::subs/title]))
        level (<sub [::subs/level])
        derived-status (issue/issue->status (<sub [::subs/issue]))]
    [:div.issue__infobox-container
     [:div.issue__infobox.is-hidden-desktop
      [sidebox-row "issue-status" (issue/derived-status->str derived-status) skeleton? (str "issue-status--" derived-status)]
      [sidebox-row "pr" (str "Pull requests: " (<sub [::subs/pr-count])) skeleton?]]
     [:div.issue__infobox.is-hidden-desktop
      [sidebox-row "contributors" (str "Contributors: " (<sub [::subs/contributor-count])) skeleton?]
      [sidebox-row (level->icon level) (str "Level: " (level->str level)) skeleton?]]
     [:div.is-hidden-desktop
      [:ul.tags
       (when-let [language (<sub [::subs/primary-language])]
         [:li.tag language])
       (when-let [compensation (<sub [::subs/compensation])]
         [:li.tag.issue__compensation.is-pulled-right compensation])]]
     [:div.issue__infobox.is-hidden-mobile
      [sidebox-row "issue-status" (issue/derived-status->str derived-status) skeleton? (str "issue-status--" derived-status)]
      [sidebox-row "pr" (str "Pull requests: " (<sub [::subs/pr-count])) skeleton?]
      [sidebox-row "contributors" (str "Contributors: " (<sub [::subs/contributor-count])) skeleton?]
      [sidebox-row (level->icon level) (str "Level: " (level->str level)) skeleton?]]
     [:ul.tags.is-hidden-mobile
      (when-not skeleton?
        [:li.tag (<sub [::subs/primary-language])])
      (when-let [compensation (<sub [::subs/compensation])]
        [:li.tag.issue__compensation.is-pulled-right compensation])]]))

(defn header [{:keys [show-like-or-edit?]
               :or   {show-like-or-edit? true}}]
  (let [skeleton? (nil? (<sub [::subs/title]))
        owner? #?(:cljs (<sub [:user/owner? (<sub [::subs/company-id])])
                  :clj  false)
        show-start-work? #?(:cljs (not (<sub [:user/owner? (<sub [::subs/company-id])]))
                            :clj  true)]
    [:div.issue__header.issue__section-content {:class (when skeleton? "skeleton")}
     [:div.issue__top-info
      {:class (when skeleton? "skeleton")}
      (if-not skeleton?
        [:a {:href (<sub [::subs/repo-url])}
         (str (<sub [::subs/repo :owner]) " / " (<sub [::subs/repo :name]))]
        [:div])
      #?(:cljs
         (when show-like-or-edit?
           (if owner?
             [icon "edit"
              :class "owner"
              :on-click #(dispatch [:edit-issue/show-issue-edit-popup (<sub [::subs/issue]) [::events/update-issue-success]])]
             #_[icon "like"])))] ;; TODO implement likes
     [:div.is-flex.issue__title-container
      (if skeleton?
        [:div.issue__logo--skeleton]
        (wrap-img img (<sub [::subs/company-logo])
                  {:alt (str (<sub [::subs/company-name]) " logo")
                   :w   64 :h 64 :class "issue__logo"}))
      [:span.issue__title {:class (when skeleton? "skeleton")} (<sub [::subs/title])]]
     [infobox]
     [:div.issue__header-buttons.columns
      (when-not skeleton?
        [:div.column
         [:a.button.button--inverted
          {:href (<sub [::subs/url]) :target "_blank" :rel "noopener"}
          "View on GitHub"]])
      (when show-start-work?
        [:div.column
         [show-popup-button]])]]))

(defn description []
  (let [body (<sub [::subs/body])
        skeleton? (nil? body)]
    (if skeleton?
      [:section.issue__description--skeleton]
      [:section.issue__description.issue__section-content
       [:h3 "Description"]
       #?(:cljs [:div.issue__description-content {:dangerouslySetInnerHTML {:__html body}}]
          :clj  [:div.issue__description-content (putil/html->hiccup body)])
       [:div.issue__labels
        (into [:ul.tags]
              (for [label (<sub [::subs/labels])]
                [:li.tag label]))]
       [:div.issue__description__view-button.is-hidden-desktop
        [:a
         {:href (<sub [::subs/url]) :target "_blank" :rel "noopener"}
         [:button.button.button--inverted "View Issue on GitHub"]]]])))

(defn author []
  (let [{:keys [name login] :as author} (<sub [::subs/author])
        org (<sub [::subs/repo :owner])
        skeleton? (nil? author)]
    [:div
     {:class (util/merge-classes
               "issue__side-pod"
               "issue__author"
               (when skeleton? "skeleton"))}
     [icon "github"]
     [:div
      [:h2.is-hidden-mobile "On GitHub"]
      [:div.github-info
       [:div.github-info__link {:key org}
        (when-not skeleton?
          [:p
           [:a.a--underlined {:href (str "https://www.github.com/" org)} org]
           [:span
            " / "
            [:a.a--underlined
             {:href (<sub [::subs/repo-url])}
             (<sub [::subs/repo :name])]]])]
       (when-not skeleton?
         [:div.github-info__repo
          [:p (<sub [::subs/repo :description]) " "
           [:br.is-hidden-desktop]
           [:a.a--underlined.github-info__more-info
            {:href (<sub [::subs/repo-url])}
            "More info >"]]])
       [:div.issue__posted-by "Issue posted by: "
        [:a.a--underlined.issue__author-login.is-hidden-desktop
         {:href (str "https://github.com/" login)}
         login]]]
      [:div.issue__author-details.is-hidden-mobile
       {:class (when skeleton? "skeleton")}
       (if skeleton?
         [:div.issue__author__github-avatar--skeleton]
         [:img.issue__author__github-avatar
          (when login {:src (str "https://avatars.githubusercontent.com/" login)})])
       [:div
        (when name
          [:p.issue__author__name name])
        [:div.issue__author__github-username
         [:a.a--underlined
          {:href (str "https://github.com/" login)}
          login]]]]]]))

(defn start-work []
  (let [{:keys [readme-url contributing-url]} (<sub [::subs/community])]
    #?(:cljs
       [popup-wrapper
        {:id           :start-work
         :on-ok        #(dispatch [::events/start-work])
         :on-close     #(dispatch [::events/show-start-work-popup false])
         :button-class (when (<sub [::subs/contribute-in-progress?]) "button--loading button--loading-white")
         :button-label (if (<sub [::subs/viewer-contributed?]) "Continue working" "Start working")}
        [:h1 "Thanks for contributing!"]
        [:p "To complete the task, just follow steps below:"]
        [:ol
         [:li [:a.a--underlined {:href (<sub [::subs/fork-url]) :target "_blank" :rel "noopener"} "Fork the repo"]]
         [:li "Clone project from " [:span.clone {:href "#"} (<sub [::subs/clone-url])]]
         (when readme-url
           [:li "You can find setup instructions in " [:a.a--underlined {:href readme-url :target "_blank" :rel "noopener"} "README"]])
         (when contributing-url
           [:li "Make sure you check " [:a.a--underlined {:href contributing-url :target "_blank" :rel "noopener"} "Contribution guidelines"]])
         [:li "When ready submit PR on GitHub and don't forget to link the issue to it"]
         [:li "Relax \u2615\ufe0f and wait for the maintainer to review your PR"]]])))

(defn activity
  []
  #_[:div
     {:class "issue__side-pod issue__activity"}])

(defn start-work-sticky
  [logged-in?]
  (let [show-start-work? #?(:cljs (not (<sub [:user/owner? (<sub [::subs/company-id])]))
                            :clj  true)]
    [:div
     {:class (util/merge-classes
               "issue__start-work-sticky"
               (if (<sub [::subs/show-cta-sticky?]) "issue__start-work-sticky--shown" "issue__start-work-sticky--hidden")
               (if logged-in?
                 "issue__start-work-sticky--logged-in"
                 "issue__start-work-sticky--logged-out"))}
     [:div.issue__start-work-sticky__inner
      [:div.issue__start-work-sticky__logo
       (if-let [logo (<sub [::subs/company-logo])]
         (wrap-img img logo {:alt (str (<sub [::subs/company-name]) " logo") :w 24 :h 24 :class "logo"})
         [icon "codi"])]
      [:div.issue__start-work-sticky__title
       #?(:cljs [ellipsis (<sub [::subs/title]) {:vcenter? true}]
          :clj  (<sub [::subs/title]))]
      [:div.issue__start-work-sticky__buttons
       [:a.button.button--inverted.is-hidden-mobile
        {:href (<sub [::subs/url]) :target "_blank" :rel "noopener"}
        "View on GitHub"]
       (when show-start-work?
         [show-popup-button])]]]))

(defn company-jobs
  []
  [:section.issue__company-jobs
   [:div.is-flex
    [:h2 "Jobs with this company"]
    #_[link "View all"
       :jobs :company-id (<sub [::subs/company-id])
       :class "a--underlined"]] ;; TODO this route doesn't exist yet
   [:div.issue__company-jobs__list
    (let [jobs (<sub [::subs/company-jobs])]
      (if jobs
        (doall
          (for [job jobs]
            ^{:key (:id job)}
            [job-card job {:public?           false
                           :liked?            (contains? (<sub [:wh.user/liked-jobs]) (:id job))
                           :user-has-applied? (some? (<sub [:wh.user/applied-jobs]))}]))
        (doall
          (for [i (range (<sub [::subs/num-related-jobs-to-show]))]
            ^{:key i}
            [job-card {:id (str "skeleton-job-" (inc i))} {:public? false}]))))]])

(defn other-issues
  []
  [:section.issue__other-issues
   [:div.is-flex
    [:h2 "Other Issues from this company"]
    [link "View all"
     :issues-for-company-id :company-id (<sub [::subs/company-id])
     :class "a--underlined"]]
   [:div
    (let [issues (<sub [::subs/company-issues])
          skeleton? (nil? (<sub [::subs/title]))]
      (if (and issues (not skeleton?))
        (doall
          (for [issue issues]
            ^{:key (:id issue)}
            [issue-card issue]))
        (doall
          (for [i (range (<sub [::subs/num-other-issues-to-show]))]
            ^{:key i}
            [issue-card {:id (str "skeleton-isssue-" (inc i))}]))))]])

(defn extract-github-user
  [urls]
  (some (fn [{:keys [url]}]
          (when (str/includes? url "github.com")
            (let [[_ user] (re-find #"github.com/(.+)" url)]
              user))) urls))

(defn contributors
  []
  (let [contributors (<sub [::subs/contributors])]
    (when (seq contributors)
      [:section.issue__contributors
       [:h2 "Contributors" [:small (str "(" (count contributors) ")")]]
       [:div.issue__section-content
        [:ul
         (for [{:keys [name id github-info other-urls]} contributors]
           ^{:key id}
           [:li.issue__contributor
            [:div.issue__contributor__name name]
            (when-let [login (or (:login github-info) (extract-github-user other-urls))]
              [:a.a--underlined.issue__contributor__gh
               {:href   (str "https://github.com/" login)
                :target "_blank"
                :rel    "noopener"}
               login])])]]])))

(defn page
  ([]
   #?(:cljs
      (r/create-class
       {:component-did-mount
        (fn [this]
          (putil/attach-on-scroll-event
           (fn [el-or-window]
             (dispatch [::events/set-show-cta-sticky? (> (or (.-scrollTop el-or-window)
                                                             (.-scrollY el-or-window)) 300)]))))
        :reagent-render
        (fn []
          (page (<sub [:user/logged-in?])))})))
  ([logged-in?]
   (let [hiw-pod (cond
                   #?(:cljs (<sub [:user/company?])
                      :clj  false)
                   [how-it-works/pod--company]
                   #?(:cljs (<sub [:user/logged-in?])
                      :clj  false)
                   [how-it-works/pod--candidate]
                   :else
                   [how-it-works/pod--basic])]
     [:div.main-container
      [:div.main.issue
       [:div.is-flex
        [:div.issue__main
         [header {:show-like-or-edit? logged-in?}] ;; TODO implement likes
         [:div.is-hidden-desktop
          [author]]
         [description]
         (when (<sub [::subs/show-contributors?])
           [contributors])
         [:div.is-hidden-desktop
          hiw-pod
          [activity]]
         [other-issues]
         (when logged-in?
           [company-jobs])]
        [:div.issue__side.is-hidden-mobile
         [author]
         hiw-pod
         [activity]]]]
      (when (<sub [::subs/start-work-popup-shown?])
        [start-work])
      #?(:cljs
         [edit-issue/edit-issue])
      [start-work-sticky logged-in?]])))