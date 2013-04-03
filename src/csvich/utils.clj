(ns csvich.utils
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(defn csv-from-file [file-name]
  (with-open [file (io/reader file-name)]
    (doall
     (csv/read-csv file :separator \; :quote \#))))

(defn to-number [s]
  {:pre [(string? s)]}
  (cond (re-seq #"^[-+]?\d*[\.,]\d*$" s)
        (Double/parseDouble (clojure.string/replace s #"," "."))
        (re-seq #"^[-+]?\d+$" s)
        (Integer/parseInt (clojure.string/replace s #"\+" ""))
        :else s))

(defn to-keyword [s]
  {:pre [(string? s)]}
  (-> s
      (.toLowerCase)
      (clojure.string/replace " " "-")
      (keyword)))

(defn validate-transform
  [[when-h when-h-op pred pred-op do-apply-key transforms]]
  (cond (not= when-h :when-header)
        [false "First argument of transform must be :when-header"]
        (not= when-h-op :info)
        [false (str when-h-op " is an invalid operator for :when-header. Valid are: :info")]
        (not (#{:contains :equals} pred))
        [false (str pred " is not a valid predicate. Valid options are :contains, :equals")]
        (not= do-apply-key :do-apply)
        [false "5th argument must be :do-apply"]
        (not (vector? transforms))
        [false "transformations must be in a vector i.e. [transform1 transform2 ..]"]
        :else [true "All validations passed"]))