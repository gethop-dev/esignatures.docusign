[![Build Status](https://app.travis-ci.com/magnetcoop/esignatures.docusign.svg?branch=master)](https://app.travis-ci.com/magnetcoop/esignatures.docusign)

# esignatures.docusign

A Library for interacting with the [Docusign eSignature API](https://developers.docusign.com/docs/esign-rest-api/) which optionally provides [Integrant](https://github.com/weavejester/integrant) initialization keys for the [Duct](https://github.com/duct-framework/duct) framework.

## Table of Contents

* [Installation](#installation)
* [Usage](#usage)
  * [Configuration](#configuration)
    * [Configuration example](#configuration-example)
  * [Obtaining DocuSign record](#obtaining-docusign-record)
    * [Using Duct](#using-duct)
    * [Not using Duct](#not-using-duct)
  * [Creating an Envelope](#creating-an-envelope)
  * [Get Envelope Signing URL](#get-envelope-signing-url)
  * [Get Envelope document](#get-envelope-document)


## Installation

[![Clojars Project](https://img.shields.io/clojars/v/coop.magnet/esignatures.docusign.svg)](https://clojars.org/coop.magnet/esignatures.docusign)

## Usage

### Configuration

To use this library add the following key to your configuration:

`:coop.magnet.esignatures/docusign`

This key expects a configuration map with two mandatory keys
These are the mandatory keys:

* `:auth-config` : A map containing DocuSign authentication details. The map contains the following mandatory keys:
  * `:integration-key`: Your DocuSign application's integration key.
  * `:user-id`: DocuSign account's UserId.
  * `:auth-service-uri`: DocuSign authentication server URL.
  * `:private-key`: DocuSign application's private RSA key.
* `:base-url`: The base URL for DocuSign API. The `/restapi` is already considered so no need to include it.
* `:account-id`: DocuSign account's ID.

These are the optional keys:
* `:timeout`: Timeout value (in milli-seconds) for an connection attempt with DocuSign API.
* `:max-retries`: If the connection attempt fails, how many retries we want to attempt before giving up.
* `:backoff-ms`: This is a vector in the form [initial-delay-ms max-delay-ms multiplier] to control the delay between each retry. The delay for nth retry will be (max (* initial-delay-ms n multiplier) max-delay-ms). If multiplier is not specified (or if it is nil), a multiplier of 2 is used. All times are in milli-seconds.

Key initialization returns a `DocuSign` record that can be used to perform the DocuSign operations described below.

#### Configuration example

Basic configuration:
```edn
  :coop.magnet.esignatures/docusign
   {:auth-config {:integration-key "5ff3dade-dd8e-4da4-a29e-f0463120a57f"
                  :user-id "3e2a655b-7d95-447b-ba26-e5b6c896fe66"
                  :auth-service-uri "https://account-d.docusign.com"
                  :private-key "DocuSign Application's Private RSA Key"}
    :base-url "https://demo.docusign.com"
    :account-id "ea1b567f-dd03-41bf-a916-64ec174c8bb6"}
```

Configuration with custom request retry policy:
```edn
  :coop.magnet.esignatures/docusign
   {:auth-config {:integration-key "5ff3dade-dd8e-4da4-a29e-f0463120a57f"
                  :user-id "3e2a655b-7d95-447b-ba26-e5b6c896fe66"
                  :auth-service-uri "https://account-d.docusign.com"
                  :private-key "DocuSign Application's Private RSA Key"}
    :base-url "https://demo.docusign.com"
    :account-id "ea1b567f-dd03-41bf-a916-64ec174c8bb6"
    :timeout 3000
    :max-retries 5
    :backoff-ms [10 500]}
```

### Obtaining a `DocuSign` record

#### Using Duct
If you are using the library as part of a [Duct](https://github.com/duct-framework/duct)-based project, adding any of the previous configurations to your `config.edn` file will perform all the steps necessary to initialize the key and return a `DocuSign` record for the associated configuration. In order to show a few interactive usages of the library, we will do all the steps manually in the REPL.

First we require the relevant namespaces:

```clj
user> (require '[integrant.core :as ig])
nil
user>
```

Next we create the configuration var holding the DocuSign integration configuration details:

```clj
user> (def config :coop.magnet.esignatures/docusign
                  {:auth-config {:integration-key "5ff3dade-dd8e-4da4-a29e-f0463120a57f"
                                 :user-id "3e2a655b-7d95-447b-ba26-e5b6c896fe66"
                                 :auth-service-uri "https://account-d.docusign.com"
                                 :private-key "DocuSign Application's Private RSA Key"}
                   :base-url "https://demo.docusign.com"
                   :account-id "ea1b567f-dd03-41bf-a916-64ec174c8bb6"})
#'user/config
user>
```

Now that we have all pieces in place, we can initialize the `:coop.magnet.esignature/docusign` Integrant key to get a `DocuSign` record. As we are doing all this from the REPL, we have to manually require `magnet.esignature.docusign` namespace, where the `init-key` multimethod for that key is defined (this is not needed when Duct takes care of initializing the key as part of the application start up):

``` clj
user> (require '[coop.magnet.esignatures.docusign :as docusign])
nil
user>
```

And we finally initialize the key with the configuration defined above, to get our `DocuSign` record:

``` clj
user> (def ds-record (ig/init-key :coop.magnet.esignatures/docusign config))
#'user/ds-record
user> ds-record
#coop.magnet.esignatures.docusign.DocuSign{:auth-config {:integration-key "5ff3dade-dd8e-4da4-a29e-f0463120a57f"
                                                         :user-id "3e2a655b-7d95-447b-ba26-e5b6c896fe66"
                                                         :auth-service-uri "https://account-d.docusign.com"
                                                         :private-key "DocuSign Application's Private RSA Key"}
                                           :base-url "https://demo.docusign.com"
                                           :account-id "ea1b567f-dd03-41bf-a916-64ec174c8bb6"}
user>
```

#### Not using Duct

```clj
user> (require '[coop.magnet.esignatures.docusign :as docusign])
user> (docusign/init-record {:auth-config {:integration-key "5ff3dade-dd8e-4da4-a29e-f0463120a57f"
                                                    :user-id "3e2a655b-7d95-447b-ba26-e5b6c896fe66"
                                                    :auth-service-uri "https://account-d.docusign.com"
                                                    :private-key "DocuSign Application's Private RSA Key"}
                             :base-url "https://demo.docusign.com"
                             :account-id "ea1b567f-dd03-41bf-a916-64ec174c8bb6"})

#coop.magnet.esignatures.docusign.DocuSign{:auth-config {:integration-key "5ff3dade-dd8e-4da4-a29e-f0463120a57f"
                                                         :user-id "3e2a655b-7d95-447b-ba26-e5b6c896fe66"
                                                         :auth-service-uri "https://account-d.docusign.com"
                                                         :private-key "DocuSign Application's Private RSA Key"}
                                           :base-url "https://demo.docusign.com"
                                           :account-id "ea1b567f-dd03-41bf-a916-64ec174c8bb6"}
```

Now that we have our `DocuSign` record, we are ready to use the methods defined by the protocols defined in `magnet.esignatures.core` namespace.

### Creating an envelope

The envelope has two mandatory sets of entities, `Documents` and `Signers`.

* `:documents`: A vector of `Document` entity, each document have the following mandatory keys:
  * `:name`: The name of the document.
  * `:file-extension`: The extension of the document. Refer to the DocuSign specification to see the supported file formats.
  * `:stream`: The document file as an `input-stream`.
* `:signers`: A vector of `Signer` entity, each signer have the following mandatory keys:
  * `:id`: the `clientUserId` of the recipient.
  * `:email`: the email of the signer.
  * `:name`: The name of the signer.

Beware that these are only the minimal set of parameters needed to create an envelope but more parameters can be especified that are supported by the [DocuSign Envelope API specification](https://developers.docusign.com/docs/esign-rest-api/reference/envelopes/envelopes/create/). The `create-envelope` is multi arity method and you can provide an additional parameter called `opts` which is a map with optional parameters.

An example of creating a sample envelope:

``` clojure
user> (create-envelope ds-record {:documents [{:name "test"
                                               :file-extension "pdf"
                                               :stream test-file}]
                                               :signers [{:id "1"
                                                          :email "lucas.sousa@magnet.coop"
                                                          :name "Lucas Sousa"}]})
{:success? true, :id "98a4c44b-55e3-4694-8ebd-a438060471ba"}
```

### Get Envelope Signing URL

To get an envelope signing URL the following parameters are mandatory:

* `envelope-id`: The envelope ID.
* `signer`: A map containing the following mandatory keys:
  * `:id`: the `clientUserId` of the recipient.
  * `:email`: the email of the signer.
  * `:name`: The name of the signer.
* `return-url`: The callback URL used by DocuSign to redirect to when the signing process finishes.

The `get-envelope-signing-url` is multi arity method and you can provide an additional parameter called `opts` which is a map with optional parameters.

An example of getting a signing URL.
``` clojure
user> (get-envelope-signing-url ds-record "98a4c44b-55e3-4694-8ebd-a438060471ba" {:id "1" :email "lucas.sousa@magnet.coop" :name "Lucas Sousa"} "http://localhost/mycallback")
{:success? true,
 :url
 "https://demo.docusign.net/Signing/MTRedeem/v1/0af17550-f0c3-4bba-a43b-ea64f11bbea2?slt=eyJ0eXAiOiJNVCIsImFsZyI6IlJTMjU2Iiwia2lkIjoiNjgxODVmZjEtNGU1MS00Y2U5LWFmMWMtNjg5ODEyMjAzMzE3In0.AQYAAAABAAMABwCA9BQqXpjZSAgAgJQmsX-Y2UgYAAEAAAAAAAAAIQCCAgAAeyJUb2tlbklkIjoiYzMyZjkwZDAtOGYyMy00MzZhLWFlNTctN2Q1MDgxZGRkZjAxIiwiRXhwaXJhdGlvbiI6IjIwMjEtMTAtMjZUMDg6NTk6MDUrMDA6MDAiLCJJc3N1ZWRBdCI6IjIwMjEtMTAtMjZUMDg6NTQ6MDUuMjMyMTQxOCswMDowMCIsIlJlc291cmNlSWQiOiI5OGE0YzQ0Yi01NWUzLTQ2OTQtOGViZC1hNDM4MDYwNDcxYmEiLCJSZXNvdXJjZXMiOiJ7XCJFbnZlbG9wZUlkXCI6XCI5OGE0YzQ0Yi01NWUzLTQ2OTQtOGViZC1hNDM4MDYwNDcxYmFcIixcIkFjdG9yVXNlcklkXCI6XCI2MjIzZjY3Yi05YzkxLTQ4NDgtOTFlYy0xNzE0NWJiNjQzMGNcIixcIlJlY2lwaWVudElkXCI6XCI0ZmM5NjIyMS0zNTczLTQwYjEtYTg1OC03MTgxYzc3MjU2MzBcIixcIkZha2VRdWVyeVN0cmluZ1wiOlwidD0xMmRjZTY4Ni03NDFlLTQ0OTItYjQxNC04NDI4MDY1NDQxNzdcIn0iLCJUb2tlblR5cGUiOjEsIkF1ZGllbmNlIjoiMjVlMDkzOTgtMDM0NC00OTBjLThlNTMtM2FiMmNhNTYyN2JmIiwiUmVkaXJlY3RVcmkiOiJodHRwczovL2RlbW8uZG9jdXNpZ24ubmV0L1NpZ25pbmcvU3RhcnRJblNlc3Npb24uYXNweCIsIkhhc2hBbGdvcml0aG0iOjAsIkhhc2hSb3VuZHMiOjAsIlRva2VuU3RhdHVzIjowLCJJc1NpbmdsZVVzZSI6ZmFsc2V9PwAAvEzcXpjZSA.uMF-mGM0cjIR38ysvA9NLXYX12xiQ5vknoWYQbJYDxvbERXhRNrfhsINgUFANBMRJJJKdRGCGqAW20QXpRmBh6e-RXLpq4hsj4O5D9XVgbeOJsZck_VJ2_yBqbPPNOUonb81Cyl9LOkJGCDPjab24c50G8w33dODUid2AEapR7iI2s6vKp6HCNSPjXTsDX_ghIQgKa1Q511N267BtUqMX6RPJbGQgzbx_dnIuT5JcQh5O7OCqHvxGjQy_LQBAClm2S7TGzmCy5D50xNZNJ6sDIAM3oBALbH3rEd2l3LQOaFMS4z1JtqtV8me1jXW0UzaoA370eUCtcsJDa5KKrUNSA"}
```

### Get Envelope Documents

To get the an envelope document(s) the following parameters are mandatory:

* `this`: The DocuSign record.
* `:envelope-id`: The envelope ID.

The `get-envelope-documents` is multi arity method and you can provide an additional parameter called `opts` which is a map with optional parameters.

An example of getting an envelops documents combined in a single PDF (which is the default behavior):

``` clojure
user> (get-envelope-documents ds-record "98a4c44b-55e3-4694-8ebd-a438060471ba" {:documentId "combined"})

{:success? true,
 :documents
 {:content-type "application/pdf",
  :content
  #object[org.httpkit.BytesInputStream 0x4125c2cc "BytesInputStream[len=190196]"]}}
```

## License

Copyright (c) 2021 Magnet S Coop.

The source code for the library is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
