(ns small-jvm-lisp.semantics
  (:use [small-jvm-lisp.grammar]
        [small-jvm-lisp.errors]
        [small-jvm-lisp.syntax]
        [small-jvm-lisp.fsm]
        ))

(defn is-sexpr? [expr]
  (vector? expr))

(defn symbol-undefined? [[global local _] sym]
  (let [legal-syms (concat (map keywordize KEYWORDS) (flatten local) global)]
    (not-any? #(= sym %) legal-syms)))

(defn check-define [[_ local _ _] sexpr]
  (let [length (count sexpr)
        name-token (second sexpr)
        body (last sexpr)]
    (cond
      (not= length 3)
      [nil nil [(str "Wrong arguments amount to define (" length ")")] nil]
      
      (-> name-token (is? symbol?) not)
      [nil nil [(str "Not a symbol (" (.value name-token) ")")] nil]

      :else [[(second sexpr)] [(second sexpr)] nil (if (is-sexpr? body) [body] nil)])))

(defn check-lambda [_ sexpr]
  (let [length (count sexpr)
        args (second sexpr)
        body (last sexpr)]
    (cond
      (not= length 3)
      [nil nil [(str "Wrong arguments amount to lambda (" length ")")] nil]
      
      (->> sexpr second (every? #(is? % symbol?)) not)
      [nil nil [(str "Wrong arguments at lambda")] nil]

      :else [nil (second sexpr) nil (if (is-sexpr? body) [body] nil)]
      )))

(defn check-quote [_ sexpr]
  (let [length (count sexpr)]
    (if (= length 2)
      [nil nil nil nil]
      [nil nil ["Wrong arguments count to quote"] nil])))

(defn check-dynamic-list [state sexpr]
  (let [f (first sexpr)
        other (rest sexpr)
        pred (fn [t] (is? t #(or (symbol? %) (keyword? %))))
        undefined-symbols (->> other
                               (filter pred)
                               (filter (partial symbol-undefined? state))
                               vec)
        sexprs (filter is-sexpr? other)]
    (cond
      (symbol-undefined? state f)
      [nil nil ["Illegal first token for s-expression"] sexprs]
      (seq undefined-symbols)
      [nil nil [(->> undefined-symbols (map #(.value %)) vec (str "Undefined symbols: "))] sexprs]
      :else
      [nil nil nil sexprs])))
  
(defn analyze-sexpr [state sexpr]
  (let [f (first sexpr)]
    (cond
      (nil? f) [nil nil ["expected a function"] nil]
      (= f :define) (check-define state sexpr)
      (= f :lambda) (check-lambda state sexpr)
      (= f :quote) (check-quote state sexpr)
      :else (check-dynamic-list state sexpr))))

(defn conj-not-empty [coll & xs]
  (loop [coll coll, [x & xx :as xs] xs]
    (cond
      (empty? xs) coll
      (nil? x) (recur coll xx)
      :else (recur (conj coll x) xx))))
        
(defn analyze-sexpr-tree [[global local errors] sexpr]
  (loop [g global, l local, e errors, s [[sexpr]]]
    (let [current-level (last s)
          current-s (last current-level)]
      (cond
        (empty? s) [g l e]
        (empty? current-level) (recur g (if (empty? l) l (pop l)) e (pop s))
        :else (let [[g2 l2 e2 s2] (analyze-sexpr [g l e s] current-s)]
                (recur (concat g g2)
                       (conj-not-empty l l2)
                       (concat e e2)
                       (-> s pop (conj-not-empty (pop current-level) s2))))))))

(defn analyze-lonely-atom [[g l e] expr]
  (->> expr
       (str "What is that? ")
       (conj e)
       (conj [g l])))

(defn analyze-expr [state expr]
  (let [analyze (if (is-sexpr? expr)
                  analyze-sexpr-tree
                  analyze-lonely-atom)]
    (analyze state expr)))

(defn raise-semantics-error [analysis]
  (->> analysis
       (str "Semantics analysis failed: " analysis)
       raise))

(defn semantics [program]
  (let [errors (->> program
                    (reduce analyze-expr [[] [] []])
                    last)]
    (if (empty? errors)
      program
      (raise-semantics-error (reduce str (map str errors))))))