(ns barebones.login.views
  (:require [barebones.login.events :as login-events]
            [barebones.login.subs :as login-subs]
            [re-frame.core :as rf]))

(defn login-page []
  (let [show-error? (rf/subscribe [::login-subs/show-error])]
    [:div.col-md-4.mx-auto.mt-5
     [:div.card
      [:div.card-header "Sign in"]
      [:div.card-body

       [:form
        [:div.form-group
         [:input#username.form-control {:placeholder "Username" :type "text"}]]

        [:div.form-group
         [:input#password.form-control {:placeholder "Password" :type "password"}]]

        [:div.form-group.form-check
         [:input#remember.form-check-input {:type "checkbox"}]
         [:label.form-check-label {:for "remember"} "Remember me"]]

        [:button.btn.btn-success.btn-block
         {:on-click (fn [evt]
                      (.preventDefault evt)
                      (rf/dispatch [::login-events/login]))}
         "Login"]]]]

     (when @show-error?
       [:div.alert.alert-warning.mt-2.text-center
        "Wrong username or password." [:br] "Guess which one was wrong!"])]))