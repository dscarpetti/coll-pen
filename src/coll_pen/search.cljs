(ns coll-pen.search
  (:require
   [cljs.reader]
   [clojure.string :as str]))

#_(defn build-search-fn [search-string]
  (let [pattern (try
                  (re-pattern search-string)
                  (catch :default e
                    (println e)
                    (re-pattern " ")))]
    (fn [el]
      (when-not (coll? el)
        (re-find pattern (pr-str el))))))

(defn search-seq [coll search-fn]
  (filter search-fn coll))

(defn search-vec [coll search-fn]
  (reduce-kv (fn [r i v]
               (if (search-fn v)
                 (conj r [i v])
                 r))
             [] coll))

(defn search-map [coll search-fn]
  (reduce-kv (fn [m k v]
               (if (search-fn k)
                 m
                 (dissoc m k)))
             coll coll))

#_(defn get-search-coll [coll-type coll search-string limit]
  (let [search-fn (build-search-fn search-string)
        search-results (case coll-type
                         :map (search-map coll search-fn)
                         :vec (search-vec coll search-fn)
                         (search-seq coll search-fn))]
    {:search/status :ok
     :search/coll-count (count coll)
     :search/result-count (count search-results)
     :search/results (take limit search-results)}))


(def regex-search ^{:coll-pen/instructions "Regex Search"}
  (fn [coll search-string]
    (let [search-fn (try
                      (let [pattern (re-pattern search-string)]
                        (fn [el] (when-not (coll? el)
                                   (re-find pattern (pr-str el)))))
                      (catch :default e
                        (js/console.log e)
                        (.-message e)))]
      (if (string? search-fn)
        search-fn #_(str "Bad Regex: " search-fn)
        (cond
          (map? coll) (search-map coll search-fn)
          (vector? coll) (search-vec coll search-fn)
          :else (search-seq coll search-fn))))))

(def subs-search ^{:coll-pen/instructions "Substring Search"}
  (fn [coll search-string]
    (let [search-fn (fn [el]
                      (when-not (coll? el)
                        (str/includes? (pr-str el) search-string)))]
      (cond
        (map? coll) (search-map coll search-fn)
        (vector? coll) (search-vec coll search-fn)
        :else (search-seq coll search-fn)))))

(def prefix-search ^{:coll-pen/instructions "Prefix Search"}
  (fn [coll search-string]
    (let [search-fn (fn [el]
                      (when-not (coll? el)
                        (str/starts-with? (pr-str el) search-string)))]
      (cond
        (map? coll) (search-map coll search-fn)
        (vector? coll) (search-vec coll search-fn)
        :else (search-seq coll search-fn)))))


(def eq-search ^{:coll-pen/instructions "Equality Search"}
  (fn [coll search-string]
    (let [search-term (try
                        (cljs.reader/read-string search-string)
                        (catch :default e
                          (js/console.log e)
                          ::error))
          search-fn (fn [el] (= el search-term))]
      (if (keyword-identical? search-term ::error)
        (str "Unable to Parse Term")
        (cond
          (map? coll) (search-map coll search-fn)
          (vector? coll) (search-vec coll search-fn)
          :else (search-seq coll search-fn))))))

(defn get-search-handler [fn-or-kw]
  (if (fn? fn-or-kw)
    fn-or-kw
    (case fn-or-kw
      :regex regex-search
      :subs subs-search
      :substr subs-search
      :substring subs-search
      :prefix prefix-search
      :eq eq-search
      := eq-search
      nil)))

(defn get-search-instructions [fn-or-string]
  (cond
    (string? fn-or-string) fn-or-string
    (fn? fn-or-string) (:coll-pen/instructions (meta fn-or-string))))
