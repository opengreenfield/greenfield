package org.greenfield

import org.greenfield.BaseController

@Mixin(BaseController)
class ProductSpecificationController {

    def delete_all(){
        ProductSpecification.executeUpdate('delete from ProductSpecification')
        def specificationOptions = SpecificationOption.list()
        specificationOptions.each { specificationOption ->
            specificationOption.products = null
            specificationOption.products.clear()
            specificationOption.save(flush:true)
        }
    }

    def count(){
        render(ProductSpecification.count())
    }


    def manage(Long id){
        authenticatedAdminProduct { adminAccount, productInstance ->
            def availableSpecifications = []

            productInstance.catalogs.each { catalog ->
                def c = Specification.createCriteria()
                def results = c.list() {
                    catalogs {
                        idEq(catalog.id)
                    }
                }

                results.each(){ specification ->
                    availableSpecifications.push(specification)
                }
            }

            availableSpecifications.unique { a, b ->
                a.id <=> b.id
            }

            [ productInstance: productInstance, availableSpecifications: availableSpecifications ]
        }
    }


    def add(Long id){
        authenticatedAdminProduct { adminAccount, productInstance ->
            def specificationOption = SpecificationOption.get(params.optionId)

            if(!specificationOption){
                flash.message = "Something went wrong while adding the specification to ${productInstance.name}"
                redirect(action: 'manage', id: productInstance.id)
                return
            }

            def specification = specificationOption.specification

            def productSpecificationsRemove = []
            if(productInstance.productSpecifications){
                productInstance.productSpecifications.each { productSpecification ->
                    if (productSpecification.specificationOption.specification.id == specification.id) {
                        productSpecificationsRemove.push(productSpecification)
                    }
                }
            }

            if(productSpecificationsRemove){
                productSpecificationsRemove.each { productSpecification ->
                    productInstance.removeFromProductSpecifications(productSpecification)
                    productSpecification.delete(flush:true)
                }
            }


            def productSpecification = new ProductSpecification()
            productSpecification.specificationOption = specificationOption
            productSpecification.product = productInstance
            productSpecification.save(flush:true)

            productInstance.addToProductSpecifications(productSpecification)
            productInstance.save(flush:true)

            specificationOption.addToProducts(productInstance)
            specificationOption.save(flush:true)

            println "**********************************"
            println "add products : " + specificationOption.products.size()
            println "**********************************"

            flash.message = "Successfully added specification to product"
            redirect(action: 'manage', id: productInstance.id)

        }
    }



    def remove(Long id){
        authenticatedAdminProduct { adminAccount, productInstance ->
            def specificationOption = SpecificationOption.get(params.optionId)

            if(!specificationOption){
                flash.message = "Something went wrong while adding the specification to ${productInstance.name}"
                redirect(action: 'manage', id: productInstance.id)
                return
            }


            if(productInstance.productSpecifications){
                productInstance.productSpecifications.each { productSpecification ->
                    if (productSpecification.specificationOption.id == specificationOption.id) {
                        productInstance.removeFromProductSpecifications(productSpecification)
                        productSpecification.delete(flush:true)

                        specificationOption.removeFromProducts(productInstance)
                        specificationOption.save(flush:true)
                    }
                }
            }


            println "**********************************"
            println "remove products : " + specificationOption.products.size()
            println "**********************************"


            flash.message = "Successfully removed specification from product"
            redirect(action: 'manage', id: productInstance.id)
        }
    }


}