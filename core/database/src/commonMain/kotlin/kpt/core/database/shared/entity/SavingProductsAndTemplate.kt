/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.shared.entity

import kpt.core.database.savings.entity.SavingProductsTemplate
import kpt.core.model.objects.organisations.ProductSavings

/**
 * Created by Rajan Maurya on 02/09/16.
 */
class SavingProductsAndTemplate(
    var mProductSavings: List<ProductSavings>,
    var mSavingProductsTemplate: SavingProductsTemplate,
) {
    fun getmProductSavings(): List<ProductSavings> {
        return mProductSavings
    }

    fun setmProductSavings(mProductSavings: List<ProductSavings>) {
        this.mProductSavings = mProductSavings
    }

    fun getmSavingProductsTemplate(): SavingProductsTemplate {
        return mSavingProductsTemplate
    }

    fun setmSavingProductsTemplate(mSavingProductsTemplate: SavingProductsTemplate) {
        this.mSavingProductsTemplate = mSavingProductsTemplate
    }

    override fun toString(): String {
        return "SavingProductsAndTemplate{" +
            "mProductSavings=" + mProductSavings +
            ", mSavingProductsTemplate=" + mSavingProductsTemplate +
            '}'
    }
}
