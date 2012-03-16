(ns clj-php.core
  (:use [clojure.inspector :only (atom?)]
        [clojure.string :only (join)]))

(declare compile-expr)
(declare compile-stmt)

(def indent "    ")
(def special-forms
  #{:if})

(defmulti compile-special-form
  (fn [expr] (keyword [(first expr) (:context (meta expr))])))

(defmethod compile-special-form [:if :stmt] [expr]
  (str "if(" (compile-expr (second expr)) "){\n"
       (compile-stmt (nth expr 2))
       "}else{\n"
       (compile-stmt (nth expr 3))
       "}"))
(defmethod compile-special-form [:if :expr] [[test if-expr else-expr]]
  (str (compile-expr if-expr) " ? " (compile-expr if-expr) " : " (compile-expr else-expr)))

(defn special-form? [expr]
  (special-forms (keyword (first expr))))

(defn compile-func-call [expr]
  (str (first expr) "(" (join ", " (map compile-expr (rest expr))) ")"))

(defn compile-php-atom [expr]
  (let [dollar (if (symbol? expr) "$" "")]
    (str dollar expr)))

(defn compile-array [expr]
  (str "array(" (join ", " (map compile-expr expr)) ")"))

(defn compile-stmt [expr]
  (str (compile-expr (with-meta expr {:context :stmt})) ";\n"))

(defn compile-expr [expr]
  (let [expr (if (not (:context expr))
               (with-meta expr {:context :expr})
               expr)
        compile-func (cond
                      (atom? expr) compile-php-atom
                      (special-form? expr) compile-special-form
                      (list? expr) compile-func-call
                      (vector? expr) compile-array)]
    (compile-func expr)))

