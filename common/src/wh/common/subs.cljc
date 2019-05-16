(ns wh.common.subs
  (:require
    [re-frame.core :refer [reg-sub]]))

;; This file should be where all 'core' subs are eventually migrated so that
;; they can be used in both client and server

;; USER

(reg-sub
  :user/logged-in?
  (fn [db _]
    (get-in db [:wh.user.db/sub-db :wh.user.db/id])))

(reg-sub
  :user/type
  (fn [db _]
    (get-in db [:wh.user.db/sub-db :wh.user.db/type])))

(reg-sub
  :user/admin?
  (fn [db _]
    (= (get-in db [:wh.user.db/sub-db :wh.user.db/type]) "admin")))

(reg-sub
  :user/company?
  (fn [db _]
    (= (get-in db [:wh.user.db/sub-db :wh.user.db/type]) "company")))

(reg-sub
  :user/candidate?
  (fn [db _]
    (= (get-in db [:wh.user.db/sub-db :wh.user.db/type]) "candidate")))

(reg-sub
  :user/email
  (fn [db _]
    (get-in db [:wh.user.db/sub-db :wh.user.db/email])))

(reg-sub
  :user/company-connected-github?
  (fn [db _]
    (get-in db [:wh.user.db/sub-db :wh.user.db/company :connected-github])))

(reg-sub
  :user/owner?
  (fn [db [_ id]]
    (= (get-in db [:wh.user.db/sub-db :wh.user.db/company-id]) id)))