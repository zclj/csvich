# csvich

A Clojure library to work with headered data.

Build headers that can contain additional info within parentesis: "Weight (kg)". The additional info can then be used to apply a set of rules to the value that the header will be applied to.

This can be a conveniant way to handle 3rd party csv-files or an easy way for a domain expert to configure how a set of data should be handled such as a csv file.

## Usage
A header transformation rule is given in the form [:when-header 'header-field' 'evaluation' 'argument' :do-apply ['transformations']

The current possible values are:
* <header-field> : :info
* <evaluation> : :contains, :equals
* <argument> : string
* <transformation> : any vector of functions

Headers are then applied to column data where data is a vector of vectors. Each of the inner vectors represents a row of column data. When headers are applied to the values any transformations associated with that header will be applied to the values and a vector of maps will be buildt. Each map entry represents one row of data and the keys are based on the headers.

### Examples
```clojure
;; build some headers
(def headers (build-headers ["Name" "Age" "Weight (kg)"]
               [:when-header :info :contains "kg" :do-apply [(partial * 1000)]])])

;; apply the headers to our data
(apply-headers headers [["Bobo" 30 70]])

;; => [{:name "Bobo" :age 30 :weight 70000}]
```

## License

Copyright © 2013 Stefan Karlsson

Distributed under the Eclipse Public License, the same as Clojure.
