(ns scicloj.clay.v1.hiccups)

(defn in-iframe [hiccup]
  [:iframe {:width       "100%"
            :height      "500px"
            :frameBorder "0"
            :srcdoc      hiccup}])
