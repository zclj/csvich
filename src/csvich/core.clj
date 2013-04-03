(ns csvich.core
  (:require [csvich.utils :as utils]))

(defn- find-cell-info [cell]
  (let [pattern #"\([a-zA-Z0-9]+\)"]
    (map #(clojure.string/replace % #"\(*\)*" "") (re-seq pattern cell))))

(defn- create-default-header [header]
  {:pre [(string? header)]}
  {:name (re-find #"\w+" header)
   :info (into #{} (find-cell-info header))
   :transform identity})

(defn- create-transform [is-valid-pred fns-to-apply]
  {:is-valid? is-valid-pred
   :do-apply-fn (apply comp (reverse fns-to-apply))})

(defn- create-transform-pred [pred-key pred-type pred-arg]
  (fn [header]
    (let [header-val (pred-key header)]
      (condp = pred-type
        :contains (contains? header-val pred-arg)
        :equals (= header-val (hash-set pred-arg))
        :else false))))

(defn- build-transforms [& {:keys [when-header contains equals do-apply] :as args}]
  {:pre [when-header (or contains equals) do-apply]}
  (let [[pred-type pred-arg] (or (find args :contains) (find args :equals))
        pred (create-transform-pred when-header pred-type pred-arg)]
    (create-transform pred do-apply)))

(defn- compose-valid-transforms [header transforms]
  (let [valid-transforms (filter #((:is-valid? %) header) transforms)]
    (when-let [trans-to-use (map :do-apply-fn valid-transforms)]    
      (assoc header :transform (apply comp trans-to-use)))))

(defn build-headers [headers & transforms]
  (let [default-headers (map create-default-header headers)
        header-transformations (map #(apply build-transforms %) (reverse transforms))]
    (for [header default-headers]
      (compose-valid-transforms header header-transformations))))

(defn apply-headers [headers data & options]
  (let [opts (apply hash-map options)
        key-fn (or (:to-keys opts) utils/to-keyword)
        head-keys (map #(key-fn (:name %)) headers)
        transforms (vec (map :transform headers)) ]
    (vec (for [row data]
           (let [transformed-vals (map #(%1 %2) transforms row)]
             (zipmap head-keys transformed-vals))))))





