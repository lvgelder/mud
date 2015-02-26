;(ns mud.routes-test
;  (:require [clojure.test :refer :all]
;            [ring.mock.request :as mock]
;            [mud.routes :refer :all]))
;
;(deftest test-app
;  (testing "main route"
;    (let [response (app (mock/request :get "/entrance"))]
;      (is (= (:status response) 200))
;      ;(is (= (:body response) "Hello World"))
;      ))
;  )
