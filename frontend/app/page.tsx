'use client'

import { motion } from 'framer-motion'
import Link from 'next/link'
import { Shield, Zap, CloudRain, IndianRupee } from 'lucide-react'

const features = [
  {
    icon: CloudRain,
    title: 'Weather Protection',
    description: 'Automatic coverage for heavy rain, floods, and extreme weather'
  },
  {
    icon: Zap,
    title: 'Instant Payouts',
    description: 'Get compensated automatically when disruptions occur'
  },
  {
    icon: IndianRupee,
    title: 'Affordable Weekly Plans',
    description: 'Starting at just ₹20/week for ₹800 coverage'
  },
  {
    icon: Shield,
    title: 'AI-Powered',
    description: 'Smart risk assessment and fraud detection'
  }
]

const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1
    }
  }
}

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: {
      duration: 0.5
    }
  }
}

export default function Home() {
  return (
    <main className="min-h-screen">
      {/* Hero Section */}
      <section className="relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-primary-600 via-primary-700 to-primary-900" />
        <div className="absolute inset-0 bg-[url('/grid.svg')] opacity-10" />
        
        <div className="relative max-w-7xl mx-auto px-4 py-20 sm:px-6 lg:px-8">
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            className="text-center"
          >
            <motion.div
              initial={{ scale: 0.8, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ delay: 0.2, duration: 0.5 }}
              className="inline-flex items-center gap-2 bg-white/10 backdrop-blur-sm px-4 py-2 rounded-full mb-8"
            >
              <Shield className="w-5 h-5 text-accent-400" />
              <span className="text-white/90 text-sm font-medium">
                AI-Powered Protection for Gig Workers
              </span>
            </motion.div>

            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold text-white mb-6">
              Protect Your Income
              <br />
              <span className="text-accent-400">From Weather Disruptions</span>
            </h1>
            
            <p className="text-lg sm:text-xl text-white/80 max-w-2xl mx-auto mb-10">
              Parametrix provides automated parametric insurance for delivery workers. 
              Get instant payouts when environmental conditions disrupt your work.
            </p>

            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
                <Link href="/register" className="btn-accent inline-block text-lg px-8 py-4">
                  Get Protected Now
                </Link>
              </motion.div>
              <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
                <Link href="/login" className="btn-secondary inline-block text-lg px-8 py-4 bg-white/10 border-white/30 text-white hover:bg-white/20">
                  Sign In
                </Link>
              </motion.div>
            </div>
          </motion.div>

          {/* Stats */}
          <motion.div
            initial={{ opacity: 0, y: 40 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.5, duration: 0.8 }}
            className="mt-20 grid grid-cols-2 md:grid-cols-4 gap-8"
          >
            {[
              { value: '₹20', label: 'Weekly Premium' },
              { value: '₹800', label: 'Coverage Limit' },
              { value: '<1hr', label: 'Payout Time' },
              { value: '100%', label: 'Automated' },
            ].map((stat, index) => (
              <div key={index} className="text-center">
                <div className="text-3xl sm:text-4xl font-bold text-white mb-2">{stat.value}</div>
                <div className="text-white/60 text-sm">{stat.label}</div>
              </div>
            ))}
          </motion.div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 px-4">
        <div className="max-w-7xl mx-auto">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5 }}
            className="text-center mb-16"
          >
            <h2 className="text-3xl sm:text-4xl font-bold text-slate-900 mb-4">
              How Parametrix Protects You
            </h2>
            <p className="text-lg text-slate-600 max-w-2xl mx-auto">
              Automated protection that works for you, so you can focus on your deliveries
            </p>
          </motion.div>

          <motion.div
            variants={containerVariants}
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            className="grid md:grid-cols-2 lg:grid-cols-4 gap-8"
          >
            {features.map((feature, index) => (
              <motion.div
                key={index}
                variants={itemVariants}
                whileHover={{ y: -5, transition: { duration: 0.2 } }}
                className="card p-6"
              >
                <div className="w-12 h-12 bg-primary-100 rounded-xl flex items-center justify-center mb-4">
                  <feature.icon className="w-6 h-6 text-primary-600" />
                </div>
                <h3 className="text-lg font-semibold text-slate-900 mb-2">
                  {feature.title}
                </h3>
                <p className="text-slate-600">
                  {feature.description}
                </p>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>

      {/* How It Works */}
      <section className="py-20 px-4 bg-slate-50">
        <div className="max-w-7xl mx-auto">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="text-center mb-16"
          >
            <h2 className="text-3xl sm:text-4xl font-bold text-slate-900 mb-4">
              How It Works
            </h2>
          </motion.div>

          <div className="grid md:grid-cols-3 gap-8">
            {[
              { step: '1', title: 'Subscribe Weekly', description: 'Pay just ₹20/week for instant coverage' },
              { step: '2', title: 'We Monitor', description: 'Our AI tracks weather conditions in your area' },
              { step: '3', title: 'Auto Payout', description: 'Get paid instantly when disruptions occur' },
            ].map((item, index) => (
              <motion.div
                key={index}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ delay: index * 0.2 }}
                className="text-center"
              >
                <div className="w-16 h-16 bg-primary-600 text-white text-2xl font-bold rounded-full flex items-center justify-center mx-auto mb-4">
                  {item.step}
                </div>
                <h3 className="text-xl font-semibold text-slate-900 mb-2">{item.title}</h3>
                <p className="text-slate-600">{item.description}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 px-4">
        <motion.div
          initial={{ opacity: 0, scale: 0.95 }}
          whileInView={{ opacity: 1, scale: 1 }}
          viewport={{ once: true }}
          className="max-w-4xl mx-auto text-center card p-12 bg-gradient-to-r from-primary-600 to-primary-700"
        >
          <h2 className="text-3xl sm:text-4xl font-bold text-white mb-4">
            Ready to Protect Your Income?
          </h2>
          <p className="text-lg text-white/80 mb-8">
            Join thousands of gig workers who trust Parametrix for their financial safety.
          </p>
          <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
            <Link href="/register" className="btn-accent inline-block text-lg px-8 py-4">
              Start Your Protection Now
            </Link>
          </motion.div>
        </motion.div>
      </section>

      {/* Footer */}
      <footer className="py-8 px-4 border-t border-slate-200">
        <div className="max-w-7xl mx-auto text-center text-slate-600">
          <p>© 2024 Parametrix. AI-Powered Insurance for Gig Workers.</p>
        </div>
      </footer>
    </main>
  )
}
