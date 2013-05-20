(ns small-jvm-lisp.parser
  (:use [small-jvm-lisp.lexer]
        [small-jvm-lisp.errors]
        [small-jvm-lisp.fsm]
        ))

(defn conj-last [stack elem]
  (conj (pop stack)
        (conj (last stack) elem)))

(defn raise-unmatched-brace [stack token]
  (raise-at-token token
                  (->> stack
                       last
                       (str "Unmatched brace, s-expression: "))))

(defn parse-sexpr [tokens]
  (loop [prev nil tokens tokens stack []]
    (let [token (first tokens)
          tokens (rest tokens)]
      (if (nil? token)
        (raise-unmatched-brace stack prev)
        (if (and (-> stack count (= 1)) (= token :RB))
          {:expr (first stack) :tokens tokens}
          (let [new-stack (case (.value token)
                            :LB (conj stack [])
                            :RB (conj-last (pop stack) (peek stack))
                            (conj-last stack token))]
            (recur token tokens new-stack)))))))
  
(defn parse-expr [tokens]
  (let [token (first tokens)
        token-value (.value token)]
    (cond
      (= token-value :LB) (parse-sexpr tokens)
      :else {:expr token :tokens (rest tokens)})))

(defn parse [tokens]
  (loop [expressions [] tokens tokens]
    (if (empty? tokens)
      expressions
      (let [{expr :expr tokens :tokens} (parse-expr tokens)]
        (recur (conj expressions expr) tokens)))))