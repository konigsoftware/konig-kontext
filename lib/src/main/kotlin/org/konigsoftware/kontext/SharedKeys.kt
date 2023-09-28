package org.konigsoftware.kontext

import io.grpc.Metadata
import io.grpc.Metadata.Key

/**
 * gRPC header ([Metadata]) key whose value contains the binary version of the Konig Kontext
 */
internal val KONIG_KONTEXT_GRPC_HEADER_KEY = Key.of("konig-kontext-bin", Metadata.BINARY_BYTE_MARSHALLER)
