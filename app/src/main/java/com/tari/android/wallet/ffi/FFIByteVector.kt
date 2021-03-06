/**
 * Copyright 2020 The Tari Project
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of
 * its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tari.android.wallet.ffi

import java.math.BigInteger

/**
 * Wrapper for native byte vector type.
 *
 * @author The Tari Development Team
 */

internal typealias FFIByteVectorPtr = Long

internal class FFIByteVector constructor(pointer: FFIByteVectorPtr) : FFIBase() {

    // region JNI

    private external fun jniGetLength(error: FFIError): Int
    private external fun jniGetAt(index: Int, error: FFIError): Int
    private external fun jniDestroy()
    private external fun jniCreate(byteArray: ByteArray, error: FFIError)

    // endregion

    private var ptr = nullptr

    init {
        ptr = pointer
    }

    constructor(hex: HexString) : this(nullptr) {
        val stringHex = hex.toString()
        if (stringHex.length < 64) {
            throw FFIException(message = "Argument's length is invalid - should be 64 but got " +
                    "${stringHex.length}\n$stringHex")
        }
        val byteArray = BigInteger(stringHex, 16).toByteArray()
        val error = FFIError()
        jniCreate(byteArray, error)
        throwIf(error)
    }

    constructor(bytes: ByteArray) : this(nullptr) {
        val error = FFIError()
        jniCreate(bytes, error)
        throwIf(error)
    }

    fun getLength(): Int {
        val error = FFIError()
        val len = jniGetLength(error)
        throwIf(error)
        return len
    }

    fun getBytes(): ByteArray {
        val length = getLength()
        val byteArray = ByteArray(length)
        for (i in 0 until length) {
            val m = getAt(i)
            byteArray[i] = m.toByte()
        }
        return byteArray
    }

    override fun toString(): String {
        return HexString(this).toString()
    }

    fun getPointer(): FFIByteVectorPtr {
        return ptr
    }

    fun getAt(index: Int): Int {
        val error = FFIError()
        val byte = jniGetAt(index, error)
        throwIf(error)
        return byte
    }

    override fun destroy() {
        jniDestroy()
    }

}
