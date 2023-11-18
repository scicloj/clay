(ns scicloj.clay.v2.tempfiles)

(def *target-path->tempfiles (atom {}))

(defn init-source! [target-path]
  (swap! *target-path->tempfiles
         assoc target-path {}))

(defn next-tempfile! [target-path value ext]
  (if-let [tempfile (-> @*target-path->tempfiles
                        (get target-path)
                        (get value))]
    tempfile
    ;; else
    (do (swap! *target-path->tempfiles
               (fn [target-path->templfiles]
                 (let [tempfiles (target-path->templfiles target-path)]
                   (if-let [tempfile (tempfiles value)]
                     target-path->templfiles
                     (-> target-path->templfiles
                         (assoc-in [target-path value]
                                   (str target-path
                                        "._files/"
                                        (count tempfiles)
                                        ext)))))))
        (-> @*target-path->tempfiles
            (get target-path)
            (get value)))))
