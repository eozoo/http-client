package org.springframework.feign.retryer;

import feign.RetryableException;

/**
 *
 * @author shanhuiming
 *
 */
public interface Retryer extends Cloneable {

    void continueOrPropagate(RetryableException e) throws Throwable;

    Retryer clone();

    Retryer NEVER_RETRY = new Retryer() {

        @Override
        public void continueOrPropagate(RetryableException e) throws Throwable {
            if(e.getCause() != null){
                throw e.getCause();
            }
            throw e;
        }

        @Override
        public Retryer clone() {
            return this;
        }
    };
}
