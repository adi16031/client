(ns wh.common.specs.issue
  (:require
    [#?(:clj  clojure.spec.alpha
        :cljs cljs.spec.alpha) :as s]
    [#?(:clj  clojure.spec.gen.alpha
        :cljs cljs.spec.gen.alpha) :as gen]
    #?(:clj [wh.integrations.leona])
    #?(:clj [wh.spec.common :as sc])
    #?(:clj [wh.spec.company])
    #?(:clj [wh.spec.user :as user])
    #?(:clj [wh.url :as url])
    [wh.common.specs.date]
    [wh.common.specs.primitives :as p]))

(s/def :wh.repo/id #?(:clj  sc/string-uuid
                      :cljs string?))
(s/def :wh.repo/github-id string?)
(s/def :wh.repo/viewer-can-administer boolean?)
(s/def :wh.repo/name string?)
(s/def :wh.repo/owner string?)
(s/def :wh.repo/description string?)
(s/def :wh.repo/primary-language (s/nilable string?))
(s/def :wh.repo/stargazers nat-int?)
(s/def :wh.repo/hook-id int?)
(s/def :wh.repo/hook-secret string?)
(s/def :wh.repo/readme-url string?)
(s/def :wh.repo/contributing-url string?)
(s/def :wh.repo/community (s/keys :opt-un [:wh.repo/readme-url
                                           :wh.repo/contributing-url]))

(s/def :wh/repo #?(:clj  (s/keys :req-un [:wh.repo/github-id
                                          :wh.repo/viewer-can-administer
                                          :wh.repo/name
                                          :wh.repo/owner
                                          :wh.repo/description
                                          :wh.repo/stargazers]
                                 :opt-un [:wh.repo/id
                                          :wh.repo/community
                                          :wh.repo/primary-language
                                          :wh.repo/hook-id
                                          :wh.repo/hook-secret
                                          :wh.repo/readme-url
                                          :wh.repo/contributing-url])
                   :cljs (s/keys :opt-un [:wh.repo/github-id
                                          :wh.repo/viewer-can-administer
                                          :wh.repo/name
                                          :wh.repo/owner
                                          :wh.repo/description
                                          :wh.repo/stargazers
                                          :wh.repo/id
                                          :wh.repo/community
                                          :wh.repo/primary-language
                                          :wh.repo/hook-id
                                          :wh.repo/hook-secret
                                          :wh.repo/readme-url
                                          :wh.repo/contributing-url])))

(s/def :wh.issue/id #?(:clj  sc/string-uuid
                       :cljs string?))
(s/def :wh.issue/github-id string?)
(s/def :wh.issue/url string?)
(s/def :wh.issue/number int?)
(s/def :wh.issue/title string?)
(s/def :wh.issue/body string?)
(s/def :wh.issue/body-html string?)
(s/def :wh.issue.label/name string?)
(s/def :wh.issue/pr-count nat-int?)
(s/def :wh.issue/label (s/keys :req-un [:wh.issue.label/name]))
(s/def :wh.issue/labels (s/coll-of :wh.issue/label))
(s/def :wh.issue/created-at :wh/date)
(s/def :wh.issue/company-id #?(:clj  (s/with-gen
                                       :leona.id/string
                                       (fn [] (gen/fmap
                                                (fn [s] (url/slugify s {}))
                                                (gen/not-empty (s/gen string?)))))
                               :cljs string?))
;; TODO: this should be just :wh/company, but that doesn't fully leonaize atm

;; TODO hack hack hack until all the specs are cljc
#?(:cljs (s/def :wh.company/id string?))
#?(:cljs (s/def :wh.company/name string?))
#?(:cljs (s/def :wh.company/logo string?))

(s/def :wh.issue/company (s/keys :req-un [:wh.company/id
                                          :wh.company/name]
                                 :opt-un [:wh.company/logo]))

(s/def :wh.issue.author/login string?)
(s/def :wh.issue.author/name string?)
(s/def :wh.issue/author (s/keys :req-un [:wh.issue.author/login]
                                :opt-un [:wh.issue.author/name]))
(s/def :wh.issue/viewer-contributed boolean?)
(s/def :wh.issue/published boolean?)
(s/def :wh.issue/status #{:open :closed})
(s/def :wh.issue.raw/status #{"open" "closed"})
(s/def :wh.issue/repo-id :wh.repo/id)
(s/def :wh.issue/repo :wh/repo)

(s/def :wh.issue.compensation/amount nat-int?)
(s/def :wh.issue.compensation/currency #{:EUR :GBP :USD :BTC :AUD :CAD :CHF :KHD :NOK :SEK :SGD})
(s/def :wh.issue.raw.compensation/currency #{"CHF" "SGD" "GBP" "BTC" "SEK" "USD" "CAD" "KHD" "EUR" "NOK" "AUD"})

(s/def :wh.issue/compensation (s/keys :req-un [:wh.issue.compensation/amount
                                               :wh.issue.compensation/currency]))

;; TODO remove this when spec is cljc
#?(:clj (s/def :wh.issue/contributor :wh.spec.user/user))
#?(:clj (s/def :wh.issue/contributors (s/coll-of :wh.issue/contributor)))

(s/def :wh.issue/level #{:beginner :intermediate :advanced})
(s/def :wh.issue.raw/level #{"beginner" "intermediate" "advanced"})

(s/def :wh/issue #?(:clj  (s/keys :req-un [:wh.issue/author
                                           :wh.issue/body
                                           :wh.issue/body-html
                                           :wh.issue/company-id
                                           :wh.issue/created-at
                                           :wh.issue/level
                                           :wh.issue/number
                                           :wh.issue/pr-count
                                           :wh.issue/title
                                           :wh.issue/url
                                           :wh.issue/github-id
                                           :wh.issue/repo-id
                                           :wh.issue/status]
                                  :opt-un [:wh.issue/company
                                           :wh.issue/compensation
                                           :wh.issue/contributors
                                           :wh.issue/id
                                           :wh.issue/labels
                                           :wh.issue/published
                                           :wh.issue/repo
                                           :wh.issue/viewer-contributed])
                    :cljs (s/keys :opt-un [:wh.issue/author
                                           :wh.issue/body
                                           :wh.issue/body-html
                                           :wh.issue/company
                                           :wh.issue/company-id
                                           :wh.issue/compensation
                                           :wh.issue/contributors
                                           :wh.issue/github-id
                                           :wh.issue/id
                                           :wh.issue/labels
                                           :wh.issue/level
                                           :wh.issue/number
                                           :wh.issue/pr-count
                                           :wh.issue/published
                                           :wh.issue/repo
                                           :wh.issue/repo-id
                                           :wh.issue/status
                                           :wh.issue/title
                                           :wh.issue/url
                                           :wh.issue/viewer-contributed])))

(s/def :wh.issue/raw #?(:clj  (-> :wh/issue
                                  (sc/replace-spec {:wh.issue/status :wh.issue.raw/status})
                                  (sc/replace-spec {:wh.issue/level :wh.issue.raw/level})
                                  (sc/replace-spec {:wh.issue.compensation/currency :wh.issue.raw.compensation/currency}))
                        :cljs :wh/issue))

(s/def :wh/issues (s/coll-of :wh/issue))

(s/def :wh.issue.organisation/name string?)
(s/def :wh.issue.organisation/avatar-url string?) ; FIXME: we need a leona-compatible, cljc-able URL spec
(s/def :wh.issue.organisation/repositories (s/coll-of :wh/repo))
(s/def :wh.issue/organisation (s/keys :req-un [:wh.issue.organisation/name
                                               :wh.issue.organisation/repositories]
                                      :opt-un [:wh.issue.organisation/avatar-url]))