;; # 📔️ Regex Dictionary
^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns dictionary
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [wordnet.core :as wordnet]
            [nextjournal.clerk :as clerk]
            [controls :refer [text-input]]))

(def dict
  (try
    (wordnet/make-dictionary "datasets/dict")
    (catch Exception e
      (throw (ex-info "You must download the WordNet database from https://wordnet.princeton.edu/download/current-version and expand it into the datasets/dict directory to run this demo." {})))))

(def words
  (->> (if (.exists (io/file "/usr/share/dict/words"))
         "/usr/share/dict/words"
         "https://gist.githubusercontent.com/wchargin/8927565/raw/d9783627c731268fb2935a731a618aa8e95cf465/words")
       slurp
       str/split-lines
       (filter #(seq (dict %)))))

{::clerk/visibility {:result :show}}

(clerk/html [:div [:span {:class "font-bold"} "Type three or more letters of a regex!"]])
^{:nextjournal.clerk/viewer text-input}
(defonce text-state (atom ""))

^::clerk/no-cache
(clerk/html
 (let [pat (re-pattern @text-state)
       filtered-words (if (> 3 (count @text-state)) []
                          (filter #(re-find pat %) words))]
   [:div
    [:div {:class "text-lg container overflow-y-auto"}
     (map #(vector :span {:class "inline-flex items-center justify-center px-2 py-1 mr-1 text-xs font-bold leading-none text-indigo-100 bg-indigo-700 rounded"} %)
          filtered-words)]
    (when (= (count filtered-words) 1)
      (let [word (first filtered-words)]
        [:div {:class "list-decimal mt-6"}
         [:h3 word]
         [:ol {:class "list-decimal mt-2"}
          (map #(vector :li (:gloss (wordnet/synset %))) (dict word))]]))]))
