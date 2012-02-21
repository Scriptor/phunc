(ns clj-php.core
  (:use [clojure.inspector :only (atom?)]
        [clojure.string :only (join)]))

(declare compile-expr)

(defn compile-func-call [expr]
  (str (first expr) "(" (join ", " (map compile-expr (rest expr))) ")"))

(defn compile-php-atom [expr]
  (let [dollar (if (symbol? expr) "$" "")]
    (str dollar expr)))

(defn compile-array [expr]
  (str "array(" (join ", " (map compile-expr expr)) ")"))

(defn compile-expr [expr]
  (let [compile-func
        (cond
         (list? expr) compile-func-call
         (atom? expr) compile-php-atom
         (vector? expr) compile-array)]
    (compile-func expr)))