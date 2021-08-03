(ns ged.core
  (:require [clojure.repl :as repl]
            [clojure.string :as str]))

(defn deep-merge [a & maps]
  (if (map? a)
    (apply merge-with deep-merge a maps)
    (apply merge-with deep-merge maps)))



(defn basic-creds
  [uname pass]
  (str "Basic " (js/btoa (str uname ":" pass))))


(defn url-search-params
  [prms & {:keys [str] :or {str false}}]
  (let [usp (js/URLSearchParams. (clj->js prms))]
    (if str
      (.toString usp)
      usp)))

(defn ->clj
  [v & {:keys [ks] :or {ks true}}]
  (js->clj v :keywordize-keys ks))

(def prettify-xml
  (fn [sourceXml]
    (let
     [xmlDoc (.parseFromString (new js/DOMParser) sourceXml "application/xml")
      xsltDoc
      (.parseFromString
       (new js/DOMParser)
       (.join
        #js
         ["<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
          "  <xsl:strip-space elements=\"*\"/>"
          "  <xsl:template match=\"para[content-style][not(text())]\">"
          "    <xsl:value-of select=\"normalize-space(.)\"/>"
          "  </xsl:template>" "  <xsl:template match=\"node()|@*\">"
          "    <xsl:copy><xsl:apply-templates select=\"node()|@*\"/></xsl:copy>"
          "  </xsl:template>" "  <xsl:output indent=\"yes\"/>"
          "</xsl:stylesheet>"]
        "\n")
       "application/xml")
      xsltProcessor (new js/XSLTProcessor)]
      (.importStylesheet xsltProcessor xsltDoc)
      (def resultDoc (.transformToDocument xsltProcessor xmlDoc))
      (def resultXml (.serializeToString (new js/XMLSerializer) resultDoc))
      resultXml)))

(defn pretty-edn
  [v]
  (with-out-str (cljs.pprint/pprint v)))

(defn pretty-json
  [v]
  (js/JSON.stringify v nil "\t"))

(defn pretty-json-str
  [v]
  (if-not (empty? v)
    (js/JSON.stringify (js/JSON.parse v) nil "\t")
    v))