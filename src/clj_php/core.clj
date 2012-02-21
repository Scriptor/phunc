(ns clj-php.core
  (:use [clojure.inspector :only (atom?)]
        [clojure.string :only (join)]))

(declare compile-expr)

(def special-forms
  #{:if})

(defmulti compile-special-form
  (fn [expr] (keyword (first expr))))

(defmethod compile-special-form :if [expr]
  (str "if(" (compile-expr (second expr)) "){\n"
       (compile-expr (nth expr 2))
       "\n}else{\n"
       (compile-expr (nth expr 3))
       "\n}"))

(defn special-form? [expr]
  (special-forms (keyword (first expr))))

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
         (atom? expr) compile-php-atom
         (special-form? expr) compile-special-form
         (list? expr) compile-func-call
         (vector? expr) compile-array)]
    (compile-func expr)))